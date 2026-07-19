# AI Foundry – Day 1–6 Repository Correction Plan for Codex

## Objective
Refactor the existing repository so it exactly matches the Day 1–6 architecture agreed during the masterclass.

## Global Rules
- Java 25
- Spring Boot 3.5.x
- Spring AI
- Hexagonal Architecture
- SOLID
- Small cohesive classes
- Do not rewrite the project from scratch.

# Phase 1 – Prompt Pipeline
Target flow:
ChatController
→ ChatUseCase
→ PromptService
→ PromptBuilder
→ PromptValidator
→ RetrievalService (optional)
→ RagContextBuilder (optional)
→ ChatProvider
→ ConversationMemory

Create/refactor:
- PromptService
- DefaultPromptService
- PromptTemplateRepository
- SpringPromptTemplateRepository
- PromptRenderer
- PromptBuilder
- PromptValidator

Refactor DefaultChatService so it never calls ChatProvider directly until PromptService builds the final prompt.

# Phase 2 – RAG Integration
When useRag=true:
1. Rewrite query.
2. Generate embedding.
3. Search vector store.
4. Build RAG context.
5. Inject context into prompt.
6. Invoke ChatProvider.

When false, skip retrieval.

# Phase 3 – Split Large Classes
Replace AgentServices with:
- Agent
- AgentDefinition
- AgentRequest
- AgentResponse
- AgentRegistry
- DefaultAgentRegistry
- IntentClassifier
- RuleBasedIntentClassifier
- AgentSupervisor

Replace ToolServices with:
- Tool
- ToolDefinition
- ToolRequest
- ToolResult
- ToolRegistry
- DefaultToolRegistry
- ToolExecutionService
- ApprovalService

Replace RagServices with:
- DocumentChunker
- TokenAwareDocumentChunker
- VectorStore
- InMemoryVectorStore
- CosineSimilarity
- Chunk
- RetrievalResult

# Phase 4 – Agent Orchestration
Supervisor flow:
Intent -> Agent -> Retrieval -> Prompt -> Tool -> Approval -> ChatProvider -> Response

Agents must use PromptService, RetrievalService and ToolExecutionService.

# Phase 5 – Tool Execution
Implement approval-aware tool execution.
Return APPROVAL_REQUIRED before executing protected tools.

# Phase 6 – Prompt Files
Remove hardcoded prompts.
Load prompts from resources/prompts/.

# Phase 7 – Spring AI Mapping
Create:
- SpringAiMessageMapper
- SpringAiResponseMapper

Map System/User/Assistant/Tool messages individually.

# Phase 8 – Streaming
Persist assistant streaming responses into conversation memory.

# Phase 9 – Dependency Injection
Keep ApplicationConfiguration limited to bean wiring.

# Phase 10 – Testing
Add tests for:
- PromptService
- RAG
- Agent routing
- Tool execution
- Approval flow
- Streaming
- Prompt loading
- Spring AI mapping

Target:
- mvn clean verify passes
- 90%+ unit coverage

# Deliverables
- Refactored repository
- Updated documentation
- GitHub Actions
- Clean commit history
