# CLAUDE.md - Item Image Auto-Collection Test Project

## Project Overview
Spring Boot test project for validating automatic item image collection functionality with multi-priority fetching and performance metrics.

## Coding Standards

### Package Structure
- Use clear, logical package organization: `controller`, `service`, `model`, `config`, `util`
- Keep domain logic in service layer, not in controllers
- Separate concerns: one class, one responsibility

### Naming Conventions
- Classes: PascalCase (e.g., `ImageCollectorService`)
- Methods: camelCase, verb-first (e.g., `fetchImageFromUrl`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- Packages: lowercase, no underscores

### Method Design
- Keep methods small (< 20 lines preferred)
- Single responsibility per method
- Use descriptive names over comments
- Return `Optional<T>` instead of null
- Use early returns to reduce nesting

### Error Handling
- Create custom exceptions for business logic errors
- Use `@ControllerAdvice` for global exception handling
- Log errors with appropriate levels (ERROR, WARN, INFO)
- Return meaningful error messages to clients
- Never expose internal stack traces to API responses

## Architecture Considerations

### Service Layer Design
- Implement interface-based services for testability
- Use dependency injection via constructor
- Keep services stateless

### Performance Optimization
- Use async processing (`CompletableFuture`) for parallel operations
- Implement proper timeout handling (strategy-specific: 50ms/200ms/300ms)
- Consider connection pooling for HTTP clients
- Measure performance at method level (manual timing sufficient)

### Priority Logic Implementation
- Use Strategy pattern for different image collection methods
- Implement clear fallback chain between priorities
- Return partial results rather than failing completely
- Track which priority level succeeded in response

## Key Implementation Guidelines

### REST API Design
- Use proper HTTP methods (POST for collection)
- Return appropriate status codes (200, 400, 500, 504)
- Include performance metrics in response body
- Version your APIs (`/api/v1/...`)

### Data Validation
- Validate all inputs at controller level using `@Valid`
- Check image formats before processing (jpg, png, gif, webp)

### External Service Integration
- Always set timeouts for external calls (50ms/200ms/300ms per strategy)
- Simple retry logic (1 retry on failure, optional)
- Handle rate limiting gracefully (basic delay)

### Testing Strategy
- Unit test service methods in isolation
- Integration test the full priority chain
- Mock external dependencies (optional for verification project)
- Test timeout and error scenarios
- Aim for reasonable coverage (focus on critical paths)

## Security Considerations (Minimal for Internal Testing)
- Validate all user inputs
- Use HTTPS for external API calls when possible
- Don't log sensitive information

## Performance Metrics to Track
- Total processing time per request
- Individual image fetch times (per strategy)
- Success/failure rate per priority level
- Image resolution and file size

## Code Quality Checklist

### Before Committing
- [ ] No hardcoded values (use configuration)
- [ ] All methods have clear return types
- [ ] Proper exception handling in place
- [ ] No unused imports or variables
- [ ] Logging added for debugging
- [ ] Unit tests written and passing

### Code Review Focus
- Business logic correctness
- Error handling completeness
- Performance implications
- Security vulnerabilities
- Code readability and maintainability

## Development Best Practices

### Configuration Management
- Use `application.yml` for all configurations
- Single profile sufficient for verification project
- Document all configuration properties

### Logging Guidelines
- Use SLF4J with Logback (Spring Boot default)
- Log entry/exit for important methods
- Use appropriate log levels (DEBUG for verification)
- Simple structured logging sufficient

### Documentation
- Write clear JavaDoc for public methods
- Document complex algorithms inline
- Keep README updated with setup instructions
- Document API contracts clearly

## Common Pitfalls to Avoid

- Don't block threads unnecessarily (use async)
- Don't ignore exception handling
- Avoid magic numbers and strings (use config)
- Don't mix business logic with infrastructure code
- Avoid circular dependencies
- **Don't over-engineer for a verification project** ⚠️

## Definition of Done (Verification Project)

- Code compiles without warnings
- Core tests pass (unit + integration)
- Performance metrics meet requirements (50ms/200ms/300ms)
- Error scenarios handled gracefully
- Basic documentation updated
- (Optional) Peer review for complex logic

---

*Focus on clean, maintainable code that clearly demonstrates the multi-priority image fetching concept with measurable performance metrics.*