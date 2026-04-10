# RAG-Powered API Knowledge Engine

An enterprise-grade Retrieval-Augmented Generation (RAG) pipeline designed to ingest, chunk, and securely retrieve internal API specifications (Swagger/OpenAPI docs) to power autonomous test generation.

## The Architectural Problem
QA engineers and SDETs waste countless hours cross-referencing outdated Confluence pages, Jira tickets, and massive Swagger JSONs to construct backend test payloads. Furthermore, feeding proprietary API documentation into public LLMs (like ChatGPT) violates enterprise data privacy policies.

## The RAG Solution
This backend orchestrator solves both the productivity and security bottlenecks by building a localized "Knowledge Engine."
1. **Secure Ingestion:** Parses raw API markdown and JSON files, splits them into semantically meaningful chunks with overlap, and converts them into dense vector embeddings.
2. **Vector Storage:** Stores the embeddings in a locally hosted vector database (ChromaDB), ensuring zero proprietary data egress.
3. **Contextual Retrieval:** When an SDET prompts the system for a test payload, the engine queries the vector store, retrieves the exact endpoint schema, and forces the LLM to generate the test *strictly* from the retrieved context.

## Enterprise Tech Stack
* **Language:** Java 17+
* **Framework:** Maven
* **AI Orchestration:** LangChain4j
* **Vector Database:** ChromaDB (via Docker)
* **LLM & Embeddings:** OpenAI SDK (Easily swappable to locally hosted Mistral/Ollama via LangChain4j interfaces)
* **Observability:** Log4j2

## System Architecture
`API Docs` -> `Document Splitter` -> `Embedding Model` -> `ChromaDB Vector Store`
`User Query` -> `Vector Search` -> `Retrieved Context + Prompt` -> `LLM` -> `Exact Test Payload`
