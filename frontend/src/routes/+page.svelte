<script lang="ts">
	import { onMount } from 'svelte';

	let backendMessage = 'Loading...';
	const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

	onMount(async () => {
		try {
			const response = await fetch(`${apiUrl}/api/hello`);
			if (!response.ok) throw new Error('Network response was not ok');
			const data = await response.json();
			backendMessage = data.message;
		} catch (error) {
			console.error('Failed to fetch:', error);
			backendMessage = 'Failed to load data from backend. Ensure you are on the Tailscale network and it is running.';
		}
	});
</script>

<main class="container">
	<div class="hero">
		<h1>Welcome to the Fullstack App</h1>
		<p>This is a custom web application built with Svelte 5 and Spring Boot 3.</p>
		
		<div class="backend-status">
			<h3>Backend Status:</h3>
			<p class="message">{backendMessage}</p>
			<small>Connected to: {apiUrl}</small>
		</div>

		<div class="actions">
			<button on:click={() => alert('Get Started clicked!')}>Get Started</button>
			<button class="secondary" on:click={() => alert('Learn More clicked!')}>Learn More</button>
		</div>
	</div>
</main>

<style>
	:global(body) {
		margin: 0;
		font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
		background-color: #f8f9fa;
		color: #333;
	}

	.container {
		display: flex;
		justify-content: center;
		align-items: center;
		min-height: 100vh;
		padding: 2rem;
		box-sizing: border-box;
	}

	.hero {
		text-align: center;
		background: white;
		padding: 3rem;
		border-radius: 12px;
		box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
		max-width: 600px;
		width: 100%;
	}

	h1 {
		color: #ff3e00;
		margin-top: 0;
		margin-bottom: 1rem;
		font-size: 2.5rem;
	}

	p {
		font-size: 1.1rem;
		line-height: 1.6;
		margin-bottom: 1.5rem;
		color: #555;
	}

	.backend-status {
		margin: 2rem 0;
		padding: 1.5rem;
		background: #f1f3f5;
		border-radius: 8px;
		border-left: 4px solid #ff3e00;
		text-align: left;
	}

	.backend-status h3 {
		margin-top: 0;
		margin-bottom: 0.5rem;
		font-size: 1.1rem;
	}

	.backend-status .message {
		font-weight: 600;
		color: #212529;
		margin-bottom: 0.5rem;
	}

	.backend-status small {
		color: #868e96;
		word-break: break-all;
	}

	.actions {
		display: flex;
		gap: 1rem;
		justify-content: center;
		margin-top: 2rem;
	}

	button {
		background-color: #ff3e00;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		font-size: 1rem;
		font-weight: 600;
		border-radius: 8px;
		cursor: pointer;
		transition: background-color 0.2s, transform 0.1s;
	}

	button:hover {
		background-color: #e03600;
	}

	button:active {
		transform: scale(0.98);
	}

	button.secondary {
		background-color: #e9ecef;
		color: #495057;
	}

	button.secondary:hover {
		background-color: #dee2e6;
	}
</style>
