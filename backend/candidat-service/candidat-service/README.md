# candidat-service

Microservice Spring Boot extrait du monolithe AWD (`awd-monolith`).

- Port : **8081**
- Base MySQL propre : **candidat_db** (script : `database/candidat_db.sql`)
- Enregistré dans Eureka (`http://localhost:8761/eureka`)
- Swagger UI : http://localhost:8081/swagger-ui.html
- Health : http://localhost:8081/actuator/health

## Démarrage

1. Créer la base : `mysql -u root -p < database/candidat_db.sql`
2. (Optionnel) Démarrer **eureka-server** sur le port 8761. Sans lui, le service démarre quand même
   mais affiche des erreurs de connexion à Eureka dans les logs.
3. `mvn spring-boot:run`

Tests (H2 en mémoire, sans MySQL ni Eureka) : `mvn test`

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET / POST | /api/candidates | Liste / création (adresse optionnelle) |
| GET / PUT / DELETE | /api/candidates/{id} | Lire / modifier / supprimer |
| PUT | /api/candidates/{id}/address/{addressId} | Associer une adresse existante |
| DELETE | /api/candidates/{id}/address | Détacher l'adresse |
| GET / POST | /api/addresses | Liste / création |
| GET / PUT / DELETE | /api/addresses/{id} | Lire / modifier / supprimer |


## Reste à faire
- Dans le monolithe, `DELETE /api/candidates/{id}` renvoyait 409 si le candidat avait des candidatures.
  Cette vérification est **retirée** ici (la table `application` n'existe plus dans cette base).
  Voir le `TODO (OpenFeign)` dans `CandidateService.delete` : elle reviendra avec un client Feign vers
  candidature-service (`GET /api/applications/exists?candidateId=`).
- Ajouter `spring-cloud-starter-openfeign` à ce moment-là.
