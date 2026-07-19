# AI Foundry — Days 1–6 Class-by-Class Implementation Guide for Codex

## 1. Codex Execution Instruction

Build the complete `ai-foundry` repository for Days 1–6.

Do not stop after creating folders or placeholder classes. Implement all classes, configuration, tests, Docker files, Kubernetes files, and documentation described below.

Primary goals:

- Java 25
- Spring Boot 3.5.x
- Spring AI
- Ollama
- Maven multi-module project
- Hexagonal architecture
- Production-ready baseline
- `mvn clean verify` must pass
- No `TODO`, `UnsupportedOperationException`, empty methods, or placeholder implementations

Repository:

```text
ai-foundry
├── pom.xml
├── platform-bom
├── platform-common
├── ai-domain
├── ai-provider-spi
├── ai-application
├── ai-provider-spring-ai
├── ai-gateway-service
├── docs
├── docker
├── k8s
└── scripts
```

Root package:

```text
com.aifoundry
```

---

# 2. Canonical Architecture

```text
Client
  ↓
REST Controller
  ↓
Application Use Case
  ↓
Prompt / Retrieval / Agent Orchestration
  ↓
Provider Port
  ↓
Spring AI Adapter
  ↓
Ollama
```

For RAG:

```text
Document
  ↓
Document Ingestion
  ↓
Chunking
  ↓
Embedding
  ↓
Vector Store
  ↓
Retriever
  ↓
Context Builder
  ↓
Chat Use Case
```

For multi-agent:

```text
User Request
  ↓
Agent Supervisor
  ↓
Intent Classification
  ↓
Specialist Agent
  ↓
Tools / RAG / Chat Provider
  ↓
Response Aggregation
```

---

# 3. Root Project

## 3.1 Root `pom.xml`

Responsibilities:

- Packaging type `pom`
- Declare all modules
- Java release 25
- Maven compiler plugin
- Maven Surefire plugin
- Maven Failsafe plugin
- Enforcer plugin
- Spotless or Checkstyle
- JaCoCo
- Spring Boot plugin management
- Dependency management imported from `platform-bom`

Required modules:

```xml
<modules>
    <module>platform-bom</module>
    <module>platform-common</module>
    <module>ai-domain</module>
    <module>ai-provider-spi</module>
    <module>ai-application</module>
    <module>ai-provider-spring-ai</module>
    <module>ai-gateway-service</module>
</modules>
```

Required quality gates:

- Java 25 enforced
- No snapshot dependencies
- Tests must fail the build when failing
- Integration tests run in verify phase

---

# 4. Module: `platform-bom`

Package: none; Maven BOM only.

## Files

### `platform-bom/pom.xml`

Responsibilities:

- Packaging `pom`
- Centralize versions for:
  - Spring Boot
  - Spring AI
  - Jackson
  - Micrometer
  - OpenTelemetry
  - Testcontainers
  - Awaitility
  - ArchUnit
  - Lombok only if absolutely necessary
- Import Spring Boot dependency BOM
- Import Spring AI BOM
- Expose versions to all modules

Codex rule:

- Never duplicate dependency versions in child modules.
- Use properties only in the BOM/root POM.

---

# 5. Module: `platform-common`

Base package:

```text
com.aifoundry.platform.common
```

Purpose:

- Shared error handling
- Correlation IDs
- API error model
- Validation utilities
- Shared observability helpers
- Shared security constants
- Common pagination/value objects where needed

## 5.1 Package `com.aifoundry.platform.common.error`

### `ErrorCode`

Type: enum

Values:

```text
VALIDATION_ERROR
INVALID_PROMPT
PROVIDER_UNAVAILABLE
PROVIDER_TIMEOUT
MODEL_NOT_FOUND
RAG_DOCUMENT_NOT_FOUND
RAG_INGESTION_FAILED
RAG_RETRIEVAL_FAILED
AGENT_NOT_FOUND
AGENT_EXECUTION_FAILED
APPROVAL_REQUIRED
UNAUTHORIZED
FORBIDDEN
INTERNAL_ERROR
```

Responsibilities:

- Stable machine-readable error codes
- Never expose Java exception class names to clients

---

### `PlatformException`

Type: abstract runtime exception

Fields:

```java
private final ErrorCode errorCode;
private final Map<String, Object> details;
```

Responsibilities:

- Base exception for all platform failures
- Require `ErrorCode`
- Optional details map
- Preserve cause

Required constructors:

```java
PlatformException(ErrorCode errorCode, String message)
PlatformException(ErrorCode errorCode, String message, Throwable cause)
PlatformException(ErrorCode errorCode, String message, Map<String, Object> details)
```

---

### `ValidationException`

Extends: `PlatformException`

Responsibilities:

- Represent input validation failures
- Default error code: `VALIDATION_ERROR`

---

### `ProviderException`

Extends: `PlatformException`

Responsibilities:

- Provider-side failures
- Store provider name and model name in details
- Factory methods:
  - `timeout`
  - `unavailable`
  - `modelNotFound`
  - `unexpected`

---

### `RagException`

Extends: `PlatformException`

Responsibilities:

- Ingestion and retrieval errors
- Include document ID or query ID where available

---

### `AgentException`

Extends: `PlatformException`

Responsibilities:

- Agent lookup and execution errors
- Include agent ID and execution ID in details

---

### `ApiError`

Type: immutable record

Fields:

```java
Instant timestamp
int status
String error
String code
String message
String path
String correlationId
Map<String, Object> details
```

Responsibilities:

- Standard error body returned by REST API

---

## 5.2 Package `com.aifoundry.platform.common.web`

### `CorrelationIdFilter`

Type: servlet filter

Responsibilities:

- Read `X-Correlation-Id`
- Generate UUID when absent
- Put correlation ID into MDC
- Add correlation ID to response header
- Clean MDC after request

Constants:

```java
HEADER_NAME = "X-Correlation-Id"
MDC_KEY = "correlationId"
```

---

### `GlobalExceptionHandler`

Type: `@RestControllerAdvice`

Responsibilities:

- Map:
  - `ValidationException` → 400
  - `ProviderException` timeout → 504
  - `ProviderException` unavailable → 503
  - `RagException` → 422 or 500 depending on error code
  - `AgentException` → 422 or 500
  - `AccessDeniedException` → 403
  - `AuthenticationException` → 401
  - unknown exception → 500
- Return `ApiError`
- Log server errors with correlation ID
- Never leak stack traces

---

## 5.3 Package `com.aifoundry.platform.common.validation`

### `ValidationResult`

Type: immutable record

Fields:

```java
boolean valid
List<String> errors
```

Factory methods:

```java
success()
failure(List<String> errors)
```

---

### `TextSanitizer`

Responsibilities:

- Trim leading/trailing spaces
- Normalize excessive whitespace
- Reject null text
- Preserve line breaks where meaningful
- Enforce max length when supplied

Methods:

```java
String sanitize(String input)
String sanitize(String input, int maxLength)
```

---

## 5.4 Package `com.aifoundry.platform.common.observability`

### `ObservationNames`

Type: final constants class

Constants:

```text
ai.chat
ai.embedding
ai.rag.ingestion
ai.rag.retrieval
ai.agent.execution
ai.tool.execution
```

---

### `MetricNames`

Type: final constants class

Constants:

```text
ai_chat_requests_total
ai_chat_latency
ai_provider_errors_total
ai_rag_documents_ingested_total
ai_rag_retrieval_latency
ai_agent_executions_total
ai_agent_failures_total
ai_tool_executions_total
```

---

# 6. Module: `ai-domain`

Base package:

```text
com.aifoundry.ai.domain
```

Purpose:

- Pure domain model
- No Spring dependencies
- No HTTP DTOs
- No provider-specific classes

---

## 6.1 Package `com.aifoundry.ai.domain.chat`

### `ChatRole`

Type: enum

Values:

```text
SYSTEM
USER
ASSISTANT
TOOL
```

---

### `ChatMessage`

Type: immutable record

Fields:

```java
ChatRole role
String content
Map<String, Object> metadata
```

Validation:

- role required
- content must not be blank
- metadata defaults to empty map

---

### `ChatRequest`

Type: immutable record

Fields:

```java
String conversationId
String model
List<ChatMessage> messages
ChatOptions options
Map<String, Object> metadata
```

Validation:

- at least one message
- model may be null to use provider default
- options default to `ChatOptions.defaults()`

---

### `ChatOptions`

Type: immutable record

Fields:

```java
Double temperature
Double topP
Integer maxTokens
List<String> stopSequences
boolean stream
```

Factory method:

```java
static ChatOptions defaults()
```

Recommended defaults:

```text
temperature = 0.2
topP = 0.9
maxTokens = 1024
stream = false
```

---

### `ChatResponse`

Type: immutable record

Fields:

```java
String responseId
String conversationId
String model
String content
TokenUsage tokenUsage
FinishReason finishReason
Map<String, Object> metadata
```

---

### `TokenUsage`

Type: immutable record

Fields:

```java
long promptTokens
long completionTokens
long totalTokens
```

---

### `FinishReason`

Type: enum

Values:

```text
STOP
LENGTH
TOOL_CALL
CONTENT_FILTER
ERROR
UNKNOWN
```

---

## 6.2 Package `com.aifoundry.ai.domain.prompt`

### `PromptTemplate`

Type: immutable entity

Fields:

```java
String templateId
String name
String version
String template
Set<String> requiredVariables
Map<String, Object> metadata
```

Responsibilities:

- Define versioned prompts
- Validate required fields
- Do not render itself

---

### `PromptContext`

Type: immutable record

Fields:

```java
Map<String, Object> variables
List<ChatMessage> conversation
List<String> retrievedContext
Map<String, Object> metadata
```

---

### `RenderedPrompt`

Type: immutable record

Fields:

```java
String templateId
String templateVersion
String content
Map<String, Object> variables
```

---

## 6.3 Package `com.aifoundry.ai.domain.embedding`

### `EmbeddingRequest`

Fields:

```java
String model
List<String> inputs
Map<String, Object> metadata
```

---

### `Embedding`

Fields:

```java
int index
List<Float> vector
```

---

### `EmbeddingResponse`

Fields:

```java
String model
List<Embedding> embeddings
TokenUsage tokenUsage
```

---

## 6.4 Package `com.aifoundry.ai.domain.rag`

### `DocumentId`

Type: value object record

Field:

```java
String value
```

Validation:

- not blank

---

### `KnowledgeDocument`

Fields:

```java
DocumentId id
String title
String content
String source
String contentType
Map<String, Object> metadata
Instant createdAt
```

---

### `DocumentChunk`

Fields:

```java
String chunkId
DocumentId documentId
String content
int sequence
int startOffset
int endOffset
Map<String, Object> metadata
```

---

### `ChunkEmbedding`

Fields:

```java
DocumentChunk chunk
List<Float> vector
```

---

### `RetrievalQuery`

Fields:

```java
String query
int topK
Double minimumScore
Map<String, Object> filters
```

Defaults:

```text
topK = 5
minimumScore = 0.0
```

---

### `RetrievedChunk`

Fields:

```java
DocumentChunk chunk
double score
```

---

### `RetrievalResult`

Fields:

```java
String queryId
String query
List<RetrievedChunk> chunks
Duration duration
```

---

## 6.5 Package `com.aifoundry.ai.domain.agent`

### `AgentId`

Type: value object record

Field:

```java
String value
```

---

### `AgentType`

Type: enum

Values:

```text
SUPERVISOR
GENERAL_BANKING
FRAUD
LOAN
CREDIT_CARD
ACCOUNT
KNOWLEDGE
```

---

### `AgentDefinition`

Fields:

```java
AgentId id
String name
AgentType type
String description
Set<String> capabilities
Set<String> allowedTools
boolean approvalRequired
```

---

### `AgentRequest`

Fields:

```java
String executionId
String conversationId
String userId
String message
Map<String, Object> context
```

---

### `AgentResponse`

Fields:

```java
String executionId
AgentId agentId
String content
AgentExecutionStatus status
List<AgentAction> actions
Map<String, Object> metadata
```

---

### `AgentExecutionStatus`

Enum:

```text
COMPLETED
FAILED
APPROVAL_REQUIRED
PARTIAL
```

---

### `AgentAction`

Fields:

```java
String actionId
String toolName
Map<String, Object> input
Map<String, Object> output
AgentActionStatus status
```

---

### `AgentActionStatus`

Enum:

```text
PLANNED
EXECUTED
FAILED
WAITING_APPROVAL
REJECTED
```

---

## 6.6 Package `com.aifoundry.ai.domain.tool`

### `ToolDefinition`

Fields:

```java
String name
String description
Map<String, Object> inputSchema
boolean approvalRequired
```

---

### `ToolRequest`

Fields:

```java
String requestId
String toolName
Map<String, Object> arguments
Map<String, Object> context
```

---

### `ToolResult`

Fields:

```java
String requestId
String toolName
boolean success
Map<String, Object> output
String errorMessage
```

---

# 7. Module: `ai-provider-spi`

Base package:

```text
com.aifoundry.ai.provider.spi
```

Purpose:

- Define provider ports
- No Spring AI dependency
- Application module depends on SPI
- Spring AI adapter implements SPI

---

## 7.1 `ChatProvider`

Methods:

```java
ChatResponse chat(ChatRequest request);
Publisher<ChatResponseChunk> stream(ChatRequest request);
String providerName();
Set<String> supportedModels();
```

Responsibilities:

- Provider-neutral chat contract
- Synchronous and streaming APIs

---

## 7.2 `ChatResponseChunk`

Fields:

```java
String responseId
String conversationId
String model
String delta
boolean completed
FinishReason finishReason
Map<String, Object> metadata
```

---

## 7.3 `EmbeddingProvider`

Methods:

```java
EmbeddingResponse embed(EmbeddingRequest request);
String providerName();
Set<String> supportedModels();
```

---

## 7.4 `ProviderHealth`

Fields:

```java
String provider
ProviderStatus status
Duration latency
Map<String, Object> details
```

---

## 7.5 `ProviderStatus`

Enum:

```text
UP
DEGRADED
DOWN
UNKNOWN
```

---

## 7.6 `AiProviderHealthIndicator`

Methods:

```java
ProviderHealth health();
```

---

# 8. Module: `ai-application`

Base package:

```text
com.aifoundry.ai.application
```

Purpose:

- Application use cases
- Prompt pipeline
- RAG orchestration
- Multi-agent orchestration
- Ports for persistence and tools

---

# 8.1 Package `com.aifoundry.ai.application.chat`

### `ChatUseCase`

Interface method:

```java
ChatResponse execute(ChatCommand command);
```

---

### `ChatStreamingUseCase`

Interface method:

```java
Publisher<ChatResponseChunk> execute(ChatCommand command);
```

---

### `ChatCommand`

Fields:

```java
String conversationId
String userId
String model
String message
ChatOptions options
boolean useRag
Map<String, Object> metadata
```

---

### `DefaultChatService`

Implements:

```text
ChatUseCase
ChatStreamingUseCase
```

Dependencies:

```text
ChatProvider
PromptService
ConversationMemoryPort
RetrievalService
ChatMetrics
```

Responsibilities:

1. Validate command.
2. Load conversation history.
3. Retrieve RAG context when `useRag=true`.
4. Build prompt.
5. Invoke provider.
6. Persist user and assistant messages.
7. Record metrics.
8. Return provider-neutral response.
9. Support streaming without duplicating the core pipeline.

---

### `ChatCommandValidator`

Method:

```java
void validate(ChatCommand command);
```

Rules:

- message not blank
- message length <= configured limit
- conversation ID optional; generate when absent
- model optional
- temperature between 0 and 2
- topP between 0 and 1
- maxTokens positive

---

# 8.2 Package `com.aifoundry.ai.application.prompt`

### `PromptService`

Methods:

```java
RenderedPrompt buildChatPrompt(
    PromptTemplate template,
    PromptContext context
);

RenderedPrompt buildRagPrompt(
    PromptTemplate template,
    PromptContext context
);
```

Dependencies:

```text
PromptBuilder
PromptValidator
PromptTemplatePort
```

Responsibilities:

- Load template
- Validate variables
- Build final prompt
- Validate result
- Return rendered prompt metadata

---

### `PromptBuilder`

Method:

```java
RenderedPrompt build(PromptTemplate template, PromptContext context);
```

Responsibilities:

- Replace variables using `{{variableName}}`
- Join retrieved context
- Add conversation history
- Never silently ignore required variables
- Ensure deterministic rendering

---

### `PromptValidator`

Methods:

```java
void validateTemplate(PromptTemplate template);
void validateContext(PromptTemplate template, PromptContext context);
void validateRenderedPrompt(RenderedPrompt prompt);
```

Checks:

- missing variables
- blank rendered prompt
- configured maximum prompt size
- disallowed control characters
- empty RAG context when RAG template requires it

---

### `PromptTemplatePort`

Methods:

```java
Optional<PromptTemplate> findById(String templateId);
PromptTemplate getRequired(String templateId);
List<PromptTemplate> findAll();
```

---

### `InMemoryPromptTemplateAdapter`

Responsibilities:

- Provide initial templates:
  - `chat-default`
  - `rag-banking`
  - `agent-supervisor`
  - `agent-fraud`
  - `agent-loan`
  - `agent-credit-card`
  - `agent-account`
- Store immutable templates
- Suitable for Days 1–6
- Easy to replace later with database-backed adapter

---

# 8.3 Package `com.aifoundry.ai.application.memory`

### `ConversationMemoryPort`

Methods:

```java
List<ChatMessage> load(String conversationId, int maxMessages);
void append(String conversationId, ChatMessage message);
void clear(String conversationId);
```

---

### `InMemoryConversationMemoryAdapter`

Responsibilities:

- Thread-safe storage
- Configurable max messages per conversation
- Evict oldest messages
- Used for local development and tests

Implementation requirement:

- Use `ConcurrentHashMap`
- Store bounded deque per conversation

---

### `ConversationMemoryService`

Methods:

```java
List<ChatMessage> getHistory(String conversationId);
void saveUserMessage(String conversationId, String content);
void saveAssistantMessage(String conversationId, String content);
void clear(String conversationId);
```

Responsibilities:

- Application-friendly wrapper
- Centralize message conversion

---

# 8.4 Package `com.aifoundry.ai.application.rag`

### `DocumentIngestionUseCase`

Method:

```java
IngestionResult ingest(IngestDocumentCommand command);
```

---

### `IngestDocumentCommand`

Fields:

```java
String documentId
String title
String content
String source
String contentType
Map<String, Object> metadata
```

---

### `IngestionResult`

Fields:

```java
DocumentId documentId
int chunksCreated
Duration duration
IngestionStatus status
```

---

### `IngestionStatus`

Enum:

```text
COMPLETED
FAILED
PARTIAL
```

---

### `DefaultDocumentIngestionService`

Dependencies:

```text
DocumentRepositoryPort
DocumentChunker
EmbeddingProvider
VectorStorePort
RagMetrics
```

Responsibilities:

1. Validate document.
2. Persist document metadata/content.
3. Split into chunks.
4. Generate embeddings in batches.
5. Store vectors and chunk metadata.
6. Return result.
7. Ensure partial failures are reported.
8. Avoid duplicate ingestion for same document unless overwrite enabled.

---

### `DocumentChunker`

Interface:

```java
List<DocumentChunk> chunk(KnowledgeDocument document);
```

---

### `TokenAwareDocumentChunker`

Configuration:

```text
chunkSize
chunkOverlap
minimumChunkLength
```

Responsibilities:

- Split paragraphs first
- Merge small paragraphs
- Create deterministic chunk IDs
- Preserve offsets
- Apply overlap
- Avoid empty chunks

---

### `DocumentRepositoryPort`

Methods:

```java
void save(KnowledgeDocument document);
Optional<KnowledgeDocument> findById(DocumentId id);
boolean exists(DocumentId id);
void delete(DocumentId id);
```

---

### `InMemoryDocumentRepositoryAdapter`

Responsibilities:

- Thread-safe local repository
- Used for Days 4–6 and tests

---

### `VectorStorePort`

Methods:

```java
void upsert(List<ChunkEmbedding> embeddings);
List<RetrievedChunk> search(RetrievalQuery query, List<Float> queryVector);
void deleteByDocumentId(DocumentId documentId);
long count();
```

---

### `InMemoryVectorStoreAdapter`

Responsibilities:

- Store vectors and chunks
- Implement cosine similarity
- Apply metadata filters
- Sort descending by score
- Return top K
- Intended for local/testing baseline

---

### `RetrievalService`

Method:

```java
RetrievalResult retrieve(RetrievalQuery query);
```

Dependencies:

```text
EmbeddingProvider
VectorStorePort
QueryRewriter
RagMetrics
```

Responsibilities:

1. Validate query.
2. Optionally rewrite query.
3. Embed query.
4. Search vector store.
5. Filter minimum score.
6. Return result with duration.

---

### `QueryRewriter`

Interface:

```java
String rewrite(String query, Map<String, Object> context);
```

---

### `NoOpQueryRewriter`

Responsibilities:

- Return original query
- Default implementation for Days 4–6

---

### `RagContextBuilder`

Method:

```java
List<String> build(RetrievalResult result);
```

Responsibilities:

- Produce compact context snippets
- Include source, title, chunk ID
- Respect configured max context characters

---

# 8.5 Package `com.aifoundry.ai.application.agent`

### `Agent`

Interface:

```java
AgentDefinition definition();
AgentResponse execute(AgentRequest request);
```

---

### `AgentRegistry`

Methods:

```java
void register(Agent agent);
Optional<Agent> find(AgentId id);
List<Agent> findByType(AgentType type);
List<AgentDefinition> definitions();
```

Responsibilities:

- Thread-safe registry
- Reject duplicate agent IDs

---

### `DefaultAgentRegistry`

Implementation of `AgentRegistry`.

---

### `AgentSupervisor`

Method:

```java
AgentResponse execute(AgentRequest request);
```

Dependencies:

```text
IntentClassifier
AgentRegistry
ApprovalService
AgentMetrics
```

Responsibilities:

1. Classify request.
2. Select specialist agent.
3. Check approval requirements.
4. Execute agent.
5. Aggregate metadata.
6. Return failure response rather than leaking exception details.
7. Use general banking agent as fallback.

---

### `IntentClassifier`

Method:

```java
AgentType classify(String message, Map<String, Object> context);
```

---

### `RuleBasedIntentClassifier`

Rules:

- fraud, stolen, suspicious, unauthorized → `FRAUD`
- loan, mortgage, EMI, interest, eligibility → `LOAN`
- card, credit card, limit, statement → `CREDIT_CARD`
- balance, account, transaction, debit → `ACCOUNT`
- otherwise → `GENERAL_BANKING`

Responsibilities:

- Deterministic baseline
- Case-insensitive
- Unit-tested
- Replaceable later with LLM classifier

---

### `AbstractBankingAgent`

Dependencies:

```text
ChatProvider
PromptService
ToolExecutionService
RetrievalService
```

Responsibilities:

- Shared execution pipeline for specialist agents
- Build agent-specific prompt
- Optional retrieval
- Optional tool execution
- Invoke provider
- Build `AgentResponse`

Protected hooks:

```java
protected abstract String promptTemplateId();
protected abstract boolean useRag();
protected abstract Set<String> allowedTools();
```

---

### `GeneralBankingAgent`

Agent ID:

```text
general-banking-agent
```

Responsibilities:

- General banking questions
- Fallback agent
- Uses RAG
- No sensitive write tools

---

### `FraudAgent`

Agent ID:

```text
fraud-agent
```

Responsibilities:

- Fraud and suspicious transaction assistance
- Uses RAG
- May invoke:
  - `transaction-lookup`
  - `freeze-card`
- `freeze-card` requires human approval
- Must provide safety-oriented response
- Must not claim an action succeeded unless tool result confirms it

---

### `LoanAgent`

Agent ID:

```text
loan-agent
```

Responsibilities:

- Loan product and eligibility guidance
- Uses RAG
- May invoke:
  - `loan-eligibility-check`
- Must clearly label illustrative calculations
- Must not present final underwriting decisions

---

### `CreditCardAgent`

Agent ID:

```text
credit-card-agent
```

Responsibilities:

- Credit card limits, statements, benefits, replacement
- Uses RAG
- May invoke:
  - `card-details`
  - `card-replacement-request`
- Replacement requires approval

---

### `AccountAgent`

Agent ID:

```text
account-agent
```

Responsibilities:

- Account details and transaction questions
- Uses RAG
- May invoke:
  - `account-summary`
  - `transaction-lookup`
- Must mask account numbers in output

---

### `KnowledgeAgent`

Agent ID:

```text
knowledge-agent
```

Responsibilities:

- RAG-only enterprise knowledge assistant
- No operational tools
- Return citations from retrieved chunks

---

# 8.6 Package `com.aifoundry.ai.application.tool`

### `ToolPort`

Methods:

```java
ToolDefinition definition();
ToolResult execute(ToolRequest request);
```

---

### `ToolRegistry`

Methods:

```java
void register(ToolPort tool);
Optional<ToolPort> find(String toolName);
List<ToolDefinition> definitions();
```

---

### `DefaultToolRegistry`

Responsibilities:

- Thread-safe registry
- Reject duplicates
- Return immutable views

---

### `ToolExecutionService`

Dependencies:

```text
ToolRegistry
ApprovalService
ToolMetrics
```

Method:

```java
ToolResult execute(ToolRequest request, Set<String> allowedTools);
```

Responsibilities:

1. Ensure tool is registered.
2. Ensure tool is allowed for selected agent.
3. Check approval requirement.
4. Execute tool.
5. Record metrics.
6. Convert exception into failed `ToolResult`.

---

### `ApprovalService`

Methods:

```java
ApprovalDecision requestApproval(ApprovalRequest request);
Optional<ApprovalDecision> findDecision(String approvalId);
```

---

### `ApprovalRequest`

Fields:

```java
String approvalId
String userId
String action
String description
Map<String, Object> payload
Instant expiresAt
```

---

### `ApprovalDecision`

Fields:

```java
String approvalId
ApprovalStatus status
String decidedBy
Instant decidedAt
String comment
```

---

### `ApprovalStatus`

Enum:

```text
PENDING
APPROVED
REJECTED
EXPIRED
```

---

### `InMemoryApprovalService`

Responsibilities:

- Store pending approval requests
- Support approve/reject operations for REST endpoints
- Reject expired approvals
- Thread-safe

---

# 8.7 Package `com.aifoundry.ai.application.tool.banking`

Create simulated tools for Days 1–6. They must not integrate with real banking systems.

### `TransactionLookupTool`

Tool name:

```text
transaction-lookup
```

Input:

```text
accountId
transactionId optional
limit optional
```

Output:

- masked account ID
- simulated transactions
- timestamps
- amounts
- status

---

### `AccountSummaryTool`

Tool name:

```text
account-summary
```

Input:

```text
accountId
```

Output:

- masked account ID
- account type
- available balance
- current balance
- currency

---

### `CardDetailsTool`

Tool name:

```text
card-details
```

Input:

```text
cardId
```

Output:

- masked card
- status
- limit
- available limit
- expiry month/year

---

### `FreezeCardTool`

Tool name:

```text
freeze-card
```

Approval required: yes

Responsibilities:

- Simulated action only
- Return action ID and status
- Never store raw card number

---

### `CardReplacementRequestTool`

Tool name:

```text
card-replacement-request
```

Approval required: yes

Responsibilities:

- Create simulated replacement request
- Return request ID

---

### `LoanEligibilityCheckTool`

Tool name:

```text
loan-eligibility-check
```

Input:

```text
monthlyIncome
monthlyDebt
requestedAmount
termMonths
```

Responsibilities:

- Simple illustrative formula
- Return estimated eligibility range
- Add disclaimer that it is not underwriting

---

# 8.8 Package `com.aifoundry.ai.application.mcp`

The project must remain usable without MCP. MCP is an adapter layer, not a dependency of core domain logic.

### `McpToolGateway`

Methods:

```java
List<ToolDefinition> discoverTools();
ToolResult invoke(ToolRequest request);
```

Responsibilities:

- Provider-neutral MCP client gateway
- No Spring AI classes in interface

---

### `McpToolPortAdapter`

Implements:

```text
ToolPort
```

Responsibilities:

- Expose discovered MCP tool through internal `ToolPort`
- Delegate invocation to `McpToolGateway`

---

### `NoOpMcpToolGateway`

Responsibilities:

- Return no tools
- Throw controlled error on invocation
- Default when MCP disabled

---

# 8.9 Package `com.aifoundry.ai.application.observability`

### `ChatMetrics`

Methods:

```java
<T> T recordChat(String provider, String model, Supplier<T> action);
void recordProviderError(String provider, String model, Throwable error);
```

---

### `RagMetrics`

Methods:

```java
<T> T recordIngestion(Supplier<T> action);
<T> T recordRetrieval(Supplier<T> action);
```

---

### `AgentMetrics`

Methods:

```java
<T> T recordExecution(String agentId, Supplier<T> action);
```

---

### `ToolMetrics`

Methods:

```java
<T> T recordExecution(String toolName, Supplier<T> action);
```

---

### `MicrometerChatMetrics`
### `MicrometerRagMetrics`
### `MicrometerAgentMetrics`
### `MicrometerToolMetrics`

Responsibilities:

- Implement above metrics ports using `MeterRegistry`
- Record counters and timers
- Keep tag cardinality bounded

---

# 9. Module: `ai-provider-spring-ai`

Base package:

```text
com.aifoundry.ai.provider.springai
```

Purpose:

- Implement provider SPI using Spring AI
- Ollama is the initial provider
- Keep provider conversion inside this module

---

## 9.1 Package `com.aifoundry.ai.provider.springai.config`

### `SpringAiProviderProperties`

Prefix:

```text
ai.foundry.provider
```

Fields:

```java
String name
String defaultChatModel
String defaultEmbeddingModel
Duration timeout
boolean enabled
```

---

### `SpringAiProviderConfiguration`

Responsibilities:

- Configure SPI beans
- Validate models
- Register provider health indicator
- Condition on provider enabled

Beans:

```text
SpringAiChatProvider
SpringAiEmbeddingProvider
SpringAiProviderHealthIndicator
```

---

### `OllamaConfigurationProperties`

Prefix:

```text
spring.ai.ollama
```

Expected configuration:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2
          temperature: 0.2
      embedding:
        options:
          model: nomic-embed-text
```

---

## 9.2 Package `com.aifoundry.ai.provider.springai.chat`

### `SpringAiChatProvider`

Implements:

```text
ChatProvider
```

Dependencies:

```text
ChatClient
SpringAiMessageMapper
SpringAiResponseMapper
SpringAiProviderProperties
```

Responsibilities:

- Convert domain request to Spring AI prompt
- Invoke `ChatClient`
- Support sync and streaming
- Map response into domain model
- Convert timeouts and provider errors into `ProviderException`
- Never expose Spring AI types outside module

---

### `SpringAiMessageMapper`

Methods:

```java
List<org.springframework.ai.chat.messages.Message> toSpringMessages(
    List<ChatMessage> messages
);
```

Responsibilities:

- Map system, user, assistant, tool roles
- Reject unsupported roles

---

### `SpringAiResponseMapper`

Methods:

```java
ChatResponse toDomain(...);
ChatResponseChunk toChunk(...);
```

Responsibilities:

- Map model output
- Map token usage where available
- Map finish reasons
- Preserve safe metadata only

---

## 9.3 Package `com.aifoundry.ai.provider.springai.embedding`

### `SpringAiEmbeddingProvider`

Implements:

```text
EmbeddingProvider
```

Dependencies:

```text
EmbeddingModel
SpringAiProviderProperties
```

Responsibilities:

- Batch embedding requests
- Map vectors
- Convert errors to `ProviderException`
- Return model name and usage metadata

---

## 9.4 Package `com.aifoundry.ai.provider.springai.health`

### `SpringAiProviderHealthIndicator`

Implements:

```text
AiProviderHealthIndicator
```

Responsibilities:

- Perform lightweight provider check
- Return `UP`, `DEGRADED`, or `DOWN`
- Record latency
- Must not run expensive generation request on every actuator call

---

# 10. Module: `ai-gateway-service`

Base package:

```text
com.aifoundry.ai.gateway
```

Purpose:

- Spring Boot executable
- REST API
- Security
- API DTOs
- Bean wiring
- Actuator
- OpenAPI

---

## 10.1 `AiGatewayApplication`

Responsibilities:

- `@SpringBootApplication`
- Enable configuration properties
- No business logic

---

## 10.2 Package `com.aifoundry.ai.gateway.config`

### `ApplicationConfiguration`

Responsibilities:

- Wire application services
- Register agents
- Register tools
- Register default prompt templates
- Register in-memory adapters for Days 1–6

---

### `WebConfiguration`

Responsibilities:

- Register `CorrelationIdFilter`
- Configure Jackson
- Configure CORS from properties
- Set API base behavior

---

### `SecurityConfiguration`

Requirements:

- Spring Security
- Two profiles:
  - `local`: permit local API access
  - `secure`: JWT resource server
- Actuator health allowed
- Sensitive endpoints require authentication
- Approval endpoints require role `APPROVER`
- Admin document ingestion requires role `AI_ADMIN`

---

### `OpenApiConfiguration`

Responsibilities:

- API title
- version
- security scheme
- endpoint groups

---

### `AsyncConfiguration`

Responsibilities:

- Configure bounded executor for ingestion and tool operations
- Use virtual threads only when supported and explicitly configured
- Avoid unbounded pools

---

## 10.3 Package `com.aifoundry.ai.gateway.api.chat`

### `ChatController`

Base path:

```text
/api/v1/chat
```

Endpoints:

```http
POST /api/v1/chat/completions
POST /api/v1/chat/stream
DELETE /api/v1/chat/conversations/{conversationId}
```

Dependencies:

```text
ChatUseCase
ChatStreamingUseCase
ConversationMemoryService
ChatApiMapper
```

---

### `ChatCompletionRequestDto`

Fields:

```java
String conversationId
String model
String message
Double temperature
Double topP
Integer maxTokens
Boolean useRag
Map<String, Object> metadata
```

---

### `ChatCompletionResponseDto`

Fields:

```java
String responseId
String conversationId
String model
String content
UsageDto usage
String finishReason
Map<String, Object> metadata
```

---

### `UsageDto`

Fields:

```java
long promptTokens
long completionTokens
long totalTokens
```

---

### `ChatApiMapper`

Responsibilities:

- DTO to `ChatCommand`
- Domain response to DTO
- No business logic

---

## 10.4 Package `com.aifoundry.ai.gateway.api.rag`

### `DocumentController`

Base path:

```text
/api/v1/knowledge/documents
```

Endpoints:

```http
POST   /api/v1/knowledge/documents
GET    /api/v1/knowledge/documents/{documentId}
DELETE /api/v1/knowledge/documents/{documentId}
POST   /api/v1/knowledge/search
```

---

### `IngestDocumentRequestDto`

Fields:

```java
String documentId
String title
String content
String source
String contentType
Map<String, Object> metadata
```

---

### `IngestDocumentResponseDto`

Fields:

```java
String documentId
int chunksCreated
String status
long durationMs
```

---

### `KnowledgeSearchRequestDto`

Fields:

```java
String query
Integer topK
Double minimumScore
Map<String, Object> filters
```

---

### `KnowledgeSearchResponseDto`

Fields:

```java
String queryId
String query
List<RetrievedChunkDto> chunks
long durationMs
```

---

### `RetrievedChunkDto`

Fields:

```java
String chunkId
String documentId
String content
double score
Map<String, Object> metadata
```

---

### `RagApiMapper`

Responsibilities:

- Map RAG DTOs and domain objects

---

## 10.5 Package `com.aifoundry.ai.gateway.api.agent`

### `AgentController`

Base path:

```text
/api/v1/agents
```

Endpoints:

```http
GET  /api/v1/agents
POST /api/v1/agents/execute
```

---

### `AgentExecutionRequestDto`

Fields:

```java
String conversationId
String userId
String message
Map<String, Object> context
```

---

### `AgentExecutionResponseDto`

Fields:

```java
String executionId
String agentId
String content
String status
List<AgentActionDto> actions
Map<String, Object> metadata
```

---

### `AgentActionDto`

Fields:

```java
String actionId
String toolName
Map<String, Object> input
Map<String, Object> output
String status
```

---

### `AgentApiMapper`

Responsibilities:

- DTO/domain mapping

---

## 10.6 Package `com.aifoundry.ai.gateway.api.approval`

### `ApprovalController`

Base path:

```text
/api/v1/approvals
```

Endpoints:

```http
GET  /api/v1/approvals/{approvalId}
POST /api/v1/approvals/{approvalId}/approve
POST /api/v1/approvals/{approvalId}/reject
```

---

### `ApprovalDecisionRequestDto`

Fields:

```java
String comment
```

---

### `ApprovalResponseDto`

Fields:

```java
String approvalId
String status
String decidedBy
Instant decidedAt
String comment
```

---

## 10.7 Package `com.aifoundry.ai.gateway.api.provider`

### `ProviderController`

Base path:

```text
/api/v1/providers
```

Endpoints:

```http
GET /api/v1/providers
GET /api/v1/providers/health
```

Responsibilities:

- Return provider name
- Supported models
- Health status
- Never expose credentials or internal base URLs

---

## 10.8 Package `com.aifoundry.ai.gateway.api.model`

### `ModelController`

Base path:

```text
/api/v1/models
```

Endpoint:

```http
GET /api/v1/models
```

Responsibilities:

- Aggregate supported chat and embedding models

---

# 11. Configuration Files

## `application.yml`

Must include:

- server port
- actuator endpoints
- management tracing
- provider config
- prompt limits
- memory limits
- RAG chunk config
- security profile defaults
- logging pattern with correlation ID

Required structure:

```yaml
server:
  port: 8080

spring:
  application:
    name: ai-gateway-service
  profiles:
    default: local
  ai:
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}

ai:
  foundry:
    provider:
      name: ollama
      default-chat-model: ${OLLAMA_CHAT_MODEL:llama3.2}
      default-embedding-model: ${OLLAMA_EMBEDDING_MODEL:nomic-embed-text}
      timeout: 60s
    prompt:
      max-characters: 50000
    memory:
      max-messages-per-conversation: 30
    rag:
      chunk-size: 1000
      chunk-overlap: 150
      minimum-chunk-length: 100
      max-context-characters: 12000

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  tracing:
    sampling:
      probability: 1.0
```

Create:

```text
application-local.yml
application-secure.yml
application-test.yml
```

---

# 12. Prompt Templates

Create templates as resources under:

```text
ai-gateway-service/src/main/resources/prompts
```

Files:

```text
chat-default.txt
rag-banking.txt
agent-supervisor.txt
agent-general-banking.txt
agent-fraud.txt
agent-loan.txt
agent-credit-card.txt
agent-account.txt
agent-knowledge.txt
```

## `chat-default.txt`

Must instruct:

- answer clearly
- do not fabricate facts
- acknowledge uncertainty
- do not claim external actions occurred

## `rag-banking.txt`

Must include placeholders:

```text
{{question}}
{{context}}
{{conversation}}
```

Must instruct:

- use only supplied context for factual banking policy answers
- cite source/chunk identifiers
- state when context is insufficient

## Specialist prompts

Must include:

- role
- allowed capabilities
- tool-use rules
- prohibited actions
- safety expectations
- approval requirements

---

# 13. Docker

## `docker/Dockerfile`

Requirements:

- Multi-stage build
- Maven build stage
- JRE runtime stage
- Non-root user
- Healthcheck
- Expose 8080
- JVM container settings
- Java 25-compatible base image

## `docker/docker-compose.yml`

Services:

```text
ollama
ai-gateway-service
prometheus
grafana
```

Optional local init service:

- pull configured Ollama models

Volumes:

- Ollama data
- Grafana data

---

# 14. Kubernetes

Create under `k8s/base`:

```text
namespace.yaml
configmap.yaml
secret-example.yaml
deployment.yaml
service.yaml
serviceaccount.yaml
hpa.yaml
pod-disruption-budget.yaml
network-policy.yaml
ingress.yaml
```

Deployment requirements:

- replicas: 2
- readiness probe
- liveness probe
- startup probe
- CPU/memory requests and limits
- non-root security context
- read-only root filesystem where possible
- environment from ConfigMap/Secret
- rolling update strategy
- topology spread constraints

Create overlays:

```text
k8s/overlays/local
k8s/overlays/dev
```

Use Kustomize.

---

# 15. Scripts

Create:

```text
scripts/run-local.sh
scripts/stop-local.sh
scripts/pull-models.sh
scripts/smoke-test.sh
scripts/build.sh
```

Requirements:

- `set -euo pipefail`
- validate dependencies
- helpful error messages
- executable bit

---

# 16. Tests

## Unit tests

Required classes:

```text
ChatCommandValidatorTest
PromptBuilderTest
PromptValidatorTest
TokenAwareDocumentChunkerTest
InMemoryVectorStoreAdapterTest
RetrievalServiceTest
RuleBasedIntentClassifierTest
DefaultAgentRegistryTest
ToolExecutionServiceTest
InMemoryApprovalServiceTest
GeneralBankingAgentTest
FraudAgentTest
LoanAgentTest
CreditCardAgentTest
AccountAgentTest
```

## Integration tests

Required classes:

```text
ChatControllerIntegrationTest
DocumentControllerIntegrationTest
AgentControllerIntegrationTest
ApprovalControllerIntegrationTest
ProviderControllerIntegrationTest
```

Requirements:

- Use Spring Boot test
- Mock provider where appropriate
- Test JSON contracts
- Test error responses
- Test correlation ID
- Test authorization in secure profile

## Architecture test

### `ArchitectureRulesTest`

Use ArchUnit.

Rules:

- domain must not depend on Spring
- application must not depend on gateway
- provider SPI must not depend on Spring AI
- gateway may depend on application, domain, common
- Spring AI adapter may depend on SPI/domain/common
- controllers must not access provider adapters directly

---

# 17. Day-by-Day Commit Plan

Codex should create logical commits.

## Day 1 — Foundation

Implement:

- root POM
- BOM
- common module
- domain module
- initial gateway skeleton
- correlation ID
- error handling
- project README

Commit:

```text
day-1: bootstrap ai-foundry multi-module architecture
```

## Day 2 — Provider SPI and Prompt Pipeline

Implement:

- provider SPI
- prompt domain
- prompt builder
- prompt validator
- template repository
- chat use-case contracts
- tests

Commit:

```text
day-2: add provider SPI and prompt pipeline
```

## Day 3 — Spring AI and Ollama

Implement:

- Spring AI adapter
- Ollama config
- chat controller
- streaming endpoint
- provider health
- tests

Commit:

```text
day-3: integrate Spring AI with Ollama
```

## Day 4 — RAG Foundation

Implement:

- document model
- chunker
- embedding provider integration
- vector store port
- in-memory vector store
- retrieval service
- RAG APIs
- tests

Commit:

```text
day-4: add banking RAG foundation
```

## Day 5 — Production Hardening

Implement:

- security profiles
- metrics
- tracing
- Docker
- Kubernetes
- integration tests
- architecture tests
- operational documentation

Commit:

```text
day-5: production hardening and deployment assets
```

## Day 6 — Multi-Agent Banking Assistant

Implement:

- agent model
- registry
- supervisor
- specialist agents
- tools
- approvals
- MCP extension points
- agent APIs
- tests

Commit:

```text
day-6: add multi-agent banking assistant
```

---

# 18. README Requirements

Root README must contain:

- project overview
- architecture diagram using Mermaid
- modules
- prerequisites
- Java 25 setup
- Ollama setup
- model pull commands
- build command
- test command
- local run command
- Docker Compose command
- sample curl commands
- security profiles
- troubleshooting
- Day 1–6 scope

Each Java module should include a short README describing:

- responsibility
- dependencies
- main packages
- extension points

---

# 19. API Acceptance Examples

## Chat

```bash
curl -X POST http://localhost:8080/api/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "demo-1",
    "message": "Explain overdraft protection",
    "useRag": true
  }'
```

## Ingest knowledge

```bash
curl -X POST http://localhost:8080/api/v1/knowledge/documents \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "overdraft-policy",
    "title": "Overdraft Policy",
    "content": "Overdraft protection details...",
    "source": "internal-policy",
    "contentType": "text/plain"
  }'
```

## Execute agent

```bash
curl -X POST http://localhost:8080/api/v1/agents/execute \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "agent-demo-1",
    "userId": "user-123",
    "message": "I see a transaction I do not recognize"
  }'
```

---

# 20. Non-Negotiable Rules for Codex

1. Do not rename modules.
2. Use root package `com.aifoundry`.
3. Do not put Spring classes in `ai-domain`.
4. Do not put Spring AI types in `ai-provider-spi`.
5. Controllers must call use cases, not provider adapters.
6. Use constructor injection only.
7. Do not use field injection.
8. Do not create placeholder methods.
9. Do not leave TODO comments.
10. Do not claim build success unless the build was actually run.
11. Run:
    ```bash
    mvn clean verify
    ```
12. Fix all compilation and test errors.
13. Run Docker image build.
14. Run smoke tests when Ollama is available.
15. Document anything that cannot be validated locally.
16. Keep all banking tools simulated.
17. Never log raw account/card values.
18. Mask sensitive data in API responses.
19. Do not invent a database dependency for Days 1–6.
20. Keep persistence ports replaceable.

---

# 21. Final Codex Completion Report

At completion Codex must output:

```text
1. Files created
2. Modules completed
3. Tests executed
4. Build result
5. Docker build result
6. Smoke test result
7. Known limitations
8. Git commits created
9. Exact commands to run locally
```

Codex must not report completion until:

```bash
mvn clean verify
```

passes.

---

# 22. GitHub Review Handoff

After Codex commits and pushes the repository:

1. Open a pull request from the Codex branch.
2. Share the repository and pull request with ChatGPT.
3. ChatGPT should review:
   - module boundaries
   - package naming
   - class responsibilities
   - Spring AI isolation
   - RAG correctness
   - agent routing
   - tool authorization
   - approval workflow
   - security
   - observability
   - tests
   - Docker/Kubernetes assets
4. ChatGPT should return:
   - blocking issues
   - major issues
   - minor issues
   - recommended fixes
   - missing Day 1–6 requirements
