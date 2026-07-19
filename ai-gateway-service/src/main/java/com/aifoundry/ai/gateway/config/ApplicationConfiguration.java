package com.aifoundry.ai.gateway.config;

import com.aifoundry.ai.application.agent.*;
import com.aifoundry.ai.application.chat.ChatCommandValidator;
import com.aifoundry.ai.application.chat.DefaultChatService;
import com.aifoundry.ai.application.mcp.McpToolGateway;
import com.aifoundry.ai.application.mcp.NoOpMcpToolGateway;
import com.aifoundry.ai.application.memory.ConversationMemoryPort;
import com.aifoundry.ai.application.memory.ConversationMemoryService;
import com.aifoundry.ai.application.memory.InMemoryConversationMemoryAdapter;
import com.aifoundry.ai.application.prompt.DefaultPromptService;
import com.aifoundry.ai.application.prompt.PromptBuilder;
import com.aifoundry.ai.application.prompt.PromptRenderer;
import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.prompt.PromptTemplateRepository;
import com.aifoundry.ai.application.prompt.PromptValidator;
import com.aifoundry.ai.application.rag.DocumentChunker;
import com.aifoundry.ai.application.rag.DocumentIngestionService;
import com.aifoundry.ai.application.rag.DocumentRepositoryPort;
import com.aifoundry.ai.application.rag.InMemoryDocumentRepositoryAdapter;
import com.aifoundry.ai.application.rag.InMemoryVectorStore;
import com.aifoundry.ai.application.rag.RagContextBuilder;
import com.aifoundry.ai.application.rag.RetrievalService;
import com.aifoundry.ai.application.rag.TokenAwareDocumentChunker;
import com.aifoundry.ai.application.rag.VectorStore;
import com.aifoundry.ai.application.tool.ApprovalService;
import com.aifoundry.ai.application.tool.BankingTools;
import com.aifoundry.ai.application.tool.DefaultToolRegistry;
import com.aifoundry.ai.application.tool.InMemoryApprovalService;
import com.aifoundry.ai.application.tool.InMemoryPendingToolRequestRepository;
import com.aifoundry.ai.application.tool.PendingToolRequestRepository;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.application.tool.ToolRegistry;
import com.aifoundry.ai.gateway.adapter.prompt.SpringPromptTemplateRepository;
import com.aifoundry.ai.provider.spi.ChatProvider;
import com.aifoundry.ai.provider.spi.EmbeddingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
  @Bean
  ConversationMemoryPort memoryPort(
      @Value("${ai.foundry.memory.max-messages-per-conversation:30}") int max) {
    return new InMemoryConversationMemoryAdapter(max);
  }

  @Bean
  ConversationMemoryService memoryService(
      ConversationMemoryPort port,
      @Value("${ai.foundry.memory.max-messages-per-conversation:30}") int max) {
    return new ConversationMemoryService(port, max);
  }

  @Bean
  ChatCommandValidator chatValidator(@Value("${ai.foundry.prompt.max-characters:50000}") int max) {
    return new ChatCommandValidator(max);
  }

  @Bean
  DefaultChatService chatService(
      ChatProvider provider,
      ConversationMemoryService memory,
      ChatCommandValidator validator,
      PromptService prompts) {
    return new DefaultChatService(provider, memory, validator, prompts);
  }

  @Bean
  DocumentRepositoryPort documentRepository() {
    return new InMemoryDocumentRepositoryAdapter();
  }

  @Bean
  DocumentChunker chunker(
      @Value("${ai.foundry.rag.chunk-size:1000}") int size,
      @Value("${ai.foundry.rag.chunk-overlap:150}") int overlap,
      @Value("${ai.foundry.rag.minimum-chunk-length:100}") int minimumLength) {
    return new TokenAwareDocumentChunker(size, overlap, minimumLength);
  }

  @Bean
  VectorStore vectorStore() {
    return new InMemoryVectorStore();
  }

  @Bean
  RetrievalService.QueryRewriter queryRewriter() {
    return new RetrievalService.NoOpQueryRewriter();
  }

  @Bean
  RetrievalService retrievalService(
      EmbeddingProvider embeddings, VectorStore vectors, RetrievalService.QueryRewriter rewriter) {
    return new RetrievalService(embeddings, vectors, rewriter);
  }

  @Bean
  DocumentIngestionService ingestionService(
      DocumentRepositoryPort documents,
      DocumentChunker chunker,
      EmbeddingProvider embeddings,
      VectorStore vectors) {
    return new DocumentIngestionService(documents, chunker, embeddings, vectors);
  }

  @Bean
  RagContextBuilder contextBuilder(
      @Value("${ai.foundry.rag.max-context-characters:12000}") int max) {
    return new RagContextBuilder(max);
  }

  @Bean
  PromptTemplateRepository promptTemplates() {
    return new SpringPromptTemplateRepository();
  }

  @Bean
  PromptRenderer promptRenderer() {
    return new PromptRenderer();
  }

  @Bean
  PromptValidator promptValidator(@Value("${ai.foundry.prompt.max-characters:50000}") int max) {
    return new PromptValidator(max);
  }

  @Bean
  PromptBuilder promptBuilder(PromptRenderer renderer, PromptValidator validator) {
    return new PromptBuilder(renderer, validator);
  }

  @Bean
  PromptService promptService(
      PromptTemplateRepository templates,
      PromptBuilder builder,
      RetrievalService retrieval,
      RagContextBuilder contextBuilder) {
    return new DefaultPromptService(templates, builder, retrieval, contextBuilder);
  }

  @Bean
  ApprovalService approvals() {
    return new InMemoryApprovalService();
  }

  @Bean
  ToolRegistry tools() {
    DefaultToolRegistry registry = new DefaultToolRegistry();
    registry.register(new BankingTools.TransactionLookupTool());
    registry.register(new BankingTools.AccountSummaryTool());
    registry.register(new BankingTools.CardDetailsTool());
    registry.register(new BankingTools.FreezeCardTool());
    registry.register(new BankingTools.CardReplacementRequestTool());
    registry.register(new BankingTools.LoanEligibilityCheckTool());
    return registry;
  }

  @Bean
  ToolExecutionService toolExecutor(
      ToolRegistry tools, ApprovalService approvals, PendingToolRequestRepository pendingRequests) {
    return new ToolExecutionService(tools, approvals, pendingRequests);
  }

  @Bean
  PendingToolRequestRepository pendingToolRequests() {
    return new InMemoryPendingToolRequestRepository();
  }

  @Bean
  McpToolGateway mcp() {
    return new NoOpMcpToolGateway();
  }

  @Bean
  AgentRegistry agents(
      PromptService prompts, ToolSelector selector, ToolExecutionService tools, ChatProvider chat) {
    DefaultAgentRegistry registry = new DefaultAgentRegistry();
    registry.register(new GeneralBankingAgent(prompts, selector, tools, chat));
    registry.register(new FraudAgent(prompts, selector, tools, chat));
    registry.register(new LoanAgent(prompts, selector, tools, chat));
    registry.register(new CreditCardAgent(prompts, selector, tools, chat));
    registry.register(new AccountAgent(prompts, selector, tools, chat));
    registry.register(new KnowledgeAgent(prompts, selector, tools, chat));
    return registry;
  }

  @Bean
  ToolSelector toolSelector() {
    return new RuleBasedToolSelector();
  }

  @Bean
  IntentClassifier intentClassifier() {
    return new RuleBasedIntentClassifier();
  }

  @Bean
  AgentSupervisor supervisor(IntentClassifier classifier, AgentRegistry agents) {
    return new AgentSupervisor(classifier, agents);
  }
}
