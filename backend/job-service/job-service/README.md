# job-service

Microservice Spring Boot extrait du monolithe AWD (`awd-monolith`).

- Port : **8082**
- Base MySQL propre : **job_db** (script : `database/job_db.sql`)
- Enregistré dans Eureka (`http://localhost:8761/eureka`)
- Swagger UI : http://localhost:8082/swagger-ui.html
- Health : http://localhost:8082/actuator/health

## Démarrage

1. Créer la base : `mysql -u root -p < database/job_db.sql`
2. (Optionnel) Démarrer **eureka-server** sur le port 8761. Sans lui, le service démarre quand même
   mais affiche des erreurs de connexion à Eureka dans les logs.
3. `mvn spring-boot:run`

Tests (H2 en mémoire, sans MySQL ni Eureka) : `mvn test`

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET / POST | /api/jobs | Liste (filtres `available`, `categoryId`) / création |
| GET / PUT / DELETE | /api/jobs/{id} | Lire / modifier / supprimer |
| PATCH | /api/jobs/{id}/availability?available= | Ouvrir / fermer un job |
| GET / POST | /api/categories | Liste / création |
| GET / PUT / DELETE | /api/categories/{id} | Lire / modifier / supprimer |
| GET | /api/categories/{id}/jobs | Jobs d'une catégorie |


## Reste à faire
- Dans le monolithe, `DELETE /api/jobs/{id}` renvoyait 409 si le job avait des candidatures.
  Cette vérification est **retirée** ici (la table `application` n'existe plus dans cette base).
  Voir le `TODO (OpenFeign)` dans `JobService.delete` : elle reviendra avec un client Feign vers
  candidature-service (`GET /api/applications/exists?jobId=`).
- Ajouter `spring-cloud-starter-openfeign` à ce moment-là.
- candidature-service aura besoin de `GET /api/jobs?ids=1,2,3` pour éviter un appel par candidature.
