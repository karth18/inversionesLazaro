// slug generator simple
function slugify(text) {
  return text.toString().toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '') // quitar acentos
    .replace(/[^a-z0-9 -]/g, '') // quitar caracteres invalidos
    .trim()
    .replace(/\s+/g, '-') // espacios -> guiones
    .replace(/-+/g, '-'); // guiones repetidos
}

function autoSlug() {
  const nameEl = document.getElementById('nombre');
  const slugEl = document.getElementById('slug');
  if (!nameEl || !slugEl) return;
  const currentSlug = slugEl.value.trim();
  const generated = slugify(nameEl.value || '');
  // Si slug vacío o coincide con slug generado anteriormente, actualizamos
  if (!currentSlug || currentSlug === slugify(currentSlug)) {
    slugEl.value = generated;
  }
}
