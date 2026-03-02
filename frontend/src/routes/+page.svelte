<script lang="ts">
	import { base } from '$app/paths';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
</script>

<main class="min-h-screen bg-base-200 flex flex-col justify-center items-center text-center relative p-4">
	<div class="absolute top-4 right-4">
		<ThemeToggle />
	</div>

	<div class="max-w-md">
		<h1 class="text-5xl font-bold mb-6">Ark Resolver</h1>
		<p class="text-xl mb-8 opacity-80">Group Party Management System</p>
		<a href="{base}/login" class="btn btn-primary btn-lg">Get Started</a>
	</div>
</main>
