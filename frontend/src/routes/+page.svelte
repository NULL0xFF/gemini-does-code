<script lang="ts">
</script>

<main class="container">
	<div class="hero">
		<h1>Welcome to the Fullstack App</h1>
		<p>This is a custom web application built with Svelte 5.</p>
		<p>We are incrementally developing it, starting with this landing page!</p>
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
