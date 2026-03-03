# Project Context

## Project Overview
This project is an incrementally developed full-stack web application. It is currently set up as a multi-project repository with distinct frontend and backend directories.

*   **Frontend:** A modern web application built with Svelte 5 and SvelteKit, utilizing TypeScript and Vite. It currently features a custom landing page.
*   **Backend:** A planned Spring Boot 3 application (currently initialized as a placeholder directory).
*   **Infrastructure:** GitHub Actions is used for Continuous Integration (CI), automatically building the frontend on pull requests to the development branch.

## Directory Structure
*   `frontend/`: Contains the SvelteKit frontend application.
    *   `src/routes/`: Contains SvelteKit pages and layouts.
    *   `package.json`: Frontend dependencies and scripts.
*   `backend/`: Contains the Spring Boot backend application (currently a placeholder).
*   `.github/workflows/`: Contains CI/CD workflow configurations.

## Building and Running

### Frontend
To develop, build, and run the frontend application, navigate to the `frontend/` directory:

```bash
cd frontend
```

*   **Install Dependencies:**
    ```bash
    npm install
    ```
*   **Start Development Server:**
    ```bash
    npm run dev
    ```
*   **Build for Production:**
    ```bash
    npm run build
    ```
*   **Preview Production Build:**
    ```bash
    npm run preview
    ```
*   **Type Checking:**
    ```bash
    npm run check
    ```

### Backend
*   **Building:** TODO (Spring Boot 3 initialization pending)
*   **Running:** TODO (Spring Boot 3 initialization pending)

## Development Conventions

*   **Git Workflow:** The project follows a standard Git Flow branching strategy with `main` for production, `dev` for active development, and `feat/*` for feature branches. Pull Requests (PRs) should target the `dev` branch.
*   **Commit Messages:** The repository strictly adheres to **Conventional Commits** format including an emoji.
    *   Format: `type: :emoji: message` (e.g., `feat: ✨ Initialize frontend project`)
*   **Continuous Integration (CI):** A GitHub Action (`frontend-ci.yml`) is configured to run automatically on any PR to the `dev` branch that includes changes in the `frontend/` directory. It ensures that the application builds successfully using Node.js 20.
*   **Code Style:** Apply [Google Style Guides](https://google.github.io/styleguide/) as the default code style for the current project.
*   **Commenting:** Add only necessary comments on source code. Do not add comments explaining updates (e.g., "// Update logic"). Always write JavaDoc style comments for required code blocks and APIs.
