package com.aifoundry.ai.gateway.config;

import com.aifoundry.ai.application.agent.*;
import com.aifoundry.ai.application.chat.*;
import com.aifoundry.ai.application.mcp.*;
import com.aifoundry.ai.application.memory.*;
import com.aifoundry.ai.application.rag.*;
import com.aifoundry.ai.application.tool.*;
import com.aifoundry.ai.provider.spi.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class ApplicationConfiguration {
  @Bean
  ConversationMemoryPort memoryPort(
      @Value("${ai.foundry.memory.max-messages-per-conversation:30}") int max) {
    return new InMemoryConversationMemoryAdapter(max);
  }

  @Bean
  ConversationMemoryService memoryService(
      ConversationMemoryPort p,
      @Value("${ai.foundry.memory.max-messages-per-conversation:30}") int max) {
    return new ConversationMemoryService(p, max);
  }

  @Bean
  ChatCommandValidator chatValidator(@Value("${ai.foundry.prompt.max-characters:50000}") int max) {
    return new ChatCommandValidator(max);
  }

  @Bean
  DefaultChatService chatService(
      ChatProvider p, ConversationMemoryService m, ChatCommandValidator v) {
    return new DefaultChatService(p, m, v);
  }

  @Bean
  DocumentRepositoryPort documentRepository() {
    return new InMemoryDocumentRepositoryAdapter();
  }

  @Bean
  RagServices.DocumentChunker chunker(
      @Value("${ai.foundry.rag.chunk-size:1000}") int size,
      @Value("${ai.foundry.rag.chunk-overlap:150}") int overlap,
      @Value("${ai.foundry.rag.minimum-chunk-length:100}") int min) {
    return new RagServices.TokenAwareDocumentChunker(size, overlap, min);
  }

  @Bean
  RagServices.VectorStore vectorStore() {
    return new RagServices.InMemoryVectorStore();
  }

  @Bean
  RetrievalService.QueryRewriter queryRewriter() {
    return new RetrievalService.NoOpQueryRewriter();
  }

  @Bean
  RetrievalService retrievalService(
      EmbeddingProvider e, RagServices.VectorStore v, RetrievalService.QueryRewriter r) {
    return new RetrievalService(e, v, r);
  }

  @Bean
  DocumentIngestionService ingestionService(
      DocumentRepositoryPort d,
      RagServices.DocumentChunker c,
      EmbeddingProvider e,
      RagServices.VectorStore v) {
    return new DocumentIngestionService(d, c, e, v);
  }

  @Bean
  RagContextBuilder contextBuilder(
      @Value("${ai.foundry.rag.max-context-characters:12000}") int max) {
    return new RagContextBuilder(max);
  }

  @Bean
  ToolServices.ApprovalService approvals() {
    return new ToolServices.ApprovalService();
  }

  @Bean
  ToolServices.Registry tools() {
    var r = new ToolServices.Registry();
    r.register(new BankingTools.TransactionLookupTool());
    r.register(new BankingTools.AccountSummaryTool());
    r.register(new BankingTools.CardDetailsTool());
    r.register(new BankingTools.FreezeCardTool());
    r.register(new BankingTools.CardReplacementRequestTool());
    r.register(new BankingTools.LoanEligibilityCheckTool());
    return r;
  }

  @Bean
  ToolServices.Executor toolExecutor(ToolServices.Registry r, ToolServices.ApprovalService a) {
    return new ToolServices.Executor(r, a);
  }

  @Bean
  McpToolGateway mcp() {
    return new NoOpMcpToolGateway();
  }

  @Bean
  AgentServices.Registry agents(ChatProvider p) {
    var r = new AgentServices.Registry();
    r.register(new BankingAgents.GeneralBankingAgent(p));
    r.register(new BankingAgents.FraudAgent(p));
    r.register(new BankingAgents.LoanAgent(p));
    r.register(new BankingAgents.CreditCardAgent(p));
    r.register(new BankingAgents.AccountAgent(p));
    r.register(new BankingAgents.KnowledgeAgent(p));
    return r;
  }

  @Bean
  AgentServices.IntentClassifier intentClassifier() {
    return new AgentServices.RuleBasedIntentClassifier();
  }

  @Bean
  BankingAgents.Supervisor supervisor(AgentServices.IntentClassifier c, AgentServices.Registry r) {
    return new BankingAgents.Supervisor(c, r);
  }
}
