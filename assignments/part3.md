---
marp: true
---

### Del 3: Order Service (VG-nivå) eller Integration Layer (Godkänt-nivå)

**För Godkänt - Integration Layer:**

- Koordinering mellan User Service och Product Service
- Enkel shopping cart funktionalitet
- Basic order processing utan avancerad logik
- Event handling för user-product interactions

**För VG-nivå - Full Order Service:**

- Komplett beställningshantering med komplex business logic
- Order validation mot User Service och Product Service
- Betalningsprocesser och order status management
- Inventory reservation och rollback vid fel
- Order history och customer analytics

## Integration mellan delarna

### Synkron kommunikation (REST APIs)

**User ↔ Product Service:**

- Product Service validerar användarbehörigheter via User Service
- User Service hämtar användarens produkthistorik från Product Service
- Shopping cart operationer som kräver omedelbar respons

**Order Service integration (VG-nivå):**

- Order Service validerar användare via User Service REST API
- Order Service kontrollerar produkttillgänglighet via Product Service REST API
- Real-time inventory updates mellan services

### Bonusuppgift

- Skapa en React frontend som kan prata med era Services.
- Helt fritt val av teknik och design.
- Ni får lov att Vibe-koda en frontend med ChatGPT, Clade mm.
- Det underlättar om ni har Swagger UI för att testa API:erna och där ni kan skicka med den informationen till AI.

## Uppgift

- Skapa ett fristående Spring Boot projekt som innehåller Order Service i ett eget GitHub-repo.
- Demo av kod och funktionalitet måndag 15/9 kl 9:00.
