# CricketPulse

A student-friendly Spring Boot cricket score dashboard with a REST API, persistent match data, ball-by-ball scoring, player figures, scorecards, and a responsive frontend.

## Requirements
- Java 21
- Maven 3.9+ (or use the Maven wrapper if you add one locally)
- Internet on the first run so Maven can download dependencies

## Run it
1. Extract the ZIP and open the `CricketPulse` folder in VS Code or IntelliJ.
2. Open a terminal in this folder.
3. Run `mvn spring-boot:run`
4. Open `http://localhost:8080`

The default profile uses an H2 file database stored under `./data`, so no database setup is required. H2 console is available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:file:./data/cricketpulse`, username `sa`, and an empty password.

## Use MySQL instead
1. Create a database in MySQL:
   `CREATE DATABASE cricketpulse;`
2. Set environment variables before starting the app:
   - Windows PowerShell: `$env:SPRING_PROFILES_ACTIVE="mysql"`
   - `$env:DB_URL="jdbc:mysql://localhost:3306/cricketpulse"`
   - `$env:DB_USER="root"`
   - `$env:DB_PASSWORD="your_mysql_password"`
3. Run `mvn spring-boot:run`.

## Main features
- Dashboard with live/scheduled/completed match cards
- Simulated live match feed with automatic polling
- Ball-by-ball scoring: runs, extras, wickets, overs, run rate, innings totals
- Add match and change match status
- Match scorecard and recent ball-by-ball timeline
- Player statistics generated from recorded deliveries
- Search and filter matches
- REST API with validation and useful error responses
- H2 persistence by default, optional MySQL profile
- Responsive layout for desktop and mobile

## REST endpoints
- `GET /api/matches`
- `GET /api/matches?status=LIVE`
- `GET /api/matches/{id}`
- `POST /api/matches`
- `PATCH /api/matches/{id}/status`
- `GET /api/matches/{id}/scorecard`
- `GET /api/matches/{id}/balls`
- `POST /api/matches/{id}/balls`
- `GET /api/players/stats`
- `GET /api/players/stats?matchId=1`
- `GET /api/summary`

## Example: create a match
```json
{
  "teamOne": "Mumbai Falcons",
  "teamTwo": "Pune Strikers",
  "venue": "Pune Cricket Ground",
  "format": "T20",
  "status": "LIVE"
}
```

## Example: record a delivery
```json
{
  "batter": "A. Sharma",
  "bowler": "R. Patil",
  "runs": 4,
  "extraType": "NONE",
  "extraRuns": 0,
  "wicket": false,
  "wicketType": ""
}
```

## Notes
This is a simulation, not an official live-data provider. It updates from the deliveries entered in the app and includes demo matches on first startup. A real external cricket feed would require a licensed API key and provider-specific integration.
