# WishDauary Engineering Rules

## Core Philosophy
- This is NOT a productivity app.
- This is a free-expression platform for children.
- Do not introduce unnecessary complexity.

## General Rules
- Keep everything MVP-focused.
- Prefer readability over abstraction.
- Avoid premature optimization.

## Architecture
- Use layered + hexagonal hybrid architecture.
- Dependency direction must go inward.
- domain must not depend on adapter or framework.
- application orchestrates use cases only.
- adapter handles IO only.

## Class Rules
- One class = one responsibility.
- Controller = HTTP only.
- Application Service = use-case orchestration.
- Domain Service = business logic.
- Repository Adapter = persistence only.

## Function Rules
- One function = one responsibility.
- No “and” in function names.
- Max 30 lines per function.
- Use early return.

## DTO Rules
- Request DTO != Response DTO != Domain Model != Entity
- Never mix layers.

## Transaction
- Transaction boundary = use case.

## Exception
- Separate business exception and system exception.
- Use explicit exception names.

## Naming
- UseCase: CreateDiaryUseCase
- Service: CreateDiaryService
- Controller: DiaryController
- DTO: CreateDiaryRequest / CreateDiaryResponse
