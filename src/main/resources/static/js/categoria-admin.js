// src/main/resources/static/js/categoria-admin.js

// Generador de slug simple y limpio
function slugify(text) {
  return text.toString().toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '') // quitar acentos
    .replace(/[^a-z0-9 -]/g, '') // quitar caracteres inválidos
    .trim()
    .replace(/\s+/g, '-') // espacios -> guiones
    .replace(/-+/g, '-'); // guiones repetidos
}

// Auto-llenar slug al escribir el nombre
function autoSlugIfNeeded() {
  const nameEl = document.getElementById('nombre');
  const slugEl = document.getElementById('slug');
  if (!nameEl || !slugEl) return;
  const currentSlug = slugEl.value.trim();
  const generated = slugify(nameEl.value || '');
  // Si el slug está vacío (nuevo) o coincide con versión generada anterior, actualizamos
  if (!currentSlug || currentSlug === slugify(currentSlug)) {
    slugEl.value = generated;
  }
}

// Validación ligera en cliente antes de enviar el formulario
function validateCategoriaForm(event) {
  const nameEl = document.getElementById('nombre');
  const slugEl = document.getElementById('slug');
  let valid = true;
  let messages = [];

  if (!nameEl || !nameEl.value.trim()) {
    valid = false;
    messages.push('El nombre es obligatorio.');
  } else if (nameEl.value.trim().length > 150) {
    valid = false;
    messages.push('El nombre debe tener máximo 150 caracteres.');
  }

  if (!slugEl || !slugEl.value.trim()) {
    valid = false;
    messages.push('El slug es obligatorio.');
  } else if (slugEl.value.trim().length > 150) {
    valid = false;
    messages.push('El slug debe tener máximo 150 caracteres.');
  } else if (!/^[a-z0-9-]+$/.test(slugEl.value.trim())) {
    valid = false;
    messages.push('El slug solo puede contener letras minúsculas, números y guiones.');
  }

  if (!valid) {
    // evitar submit
    event.preventDefault();
    // mostrar errores en página (si existe contenedor), si no -> alert
    const container = document.getElementById('categoriaErrors');
    const html = messages.map(m => `<div class="alert alert-danger" role="alert">${m}</div>`).join('');
    if (container) {
      container.innerHTML = html;
      window.scrollTo({ top: container.offsetTop - 20, behavior: 'smooth' });
    } else {
      alert(messages.join('\n'));
    }
    return false;
  }
  return true;
}

// Inicializador: engancha eventos
document.addEventListener('DOMContentLoaded', function() {
  const nameEl = document.getElementById('nombre');
  const formEl = document.querySelector('form[th\\:object], form'); // intenta seleccionar el form Thymeleaf o cualquiera
  if (nameEl) nameEl.addEventListener('input', autoSlugIfNeeded);

  // Si existe campo slug y usuario lo edita manualmente, no lo sobreescribimos
  const slugEl = document.getElementById('slug');
  if (slugEl) {
    slugEl.addEventListener('input', function() {
      // ninguna acción adicional por ahora
    });
  }

  if (formEl) {
    formEl.addEventListener('submit', validateCategoriaForm);
  }

  // Contenedor de errores (creamos si no existe para mostrarlos)
  if (!document.getElementById('categoriaErrors')) {
    const container = document.createElement('div');
    container.id = 'categoriaErrors';
    const main = document.querySelector('.container') || document.body;
    main.insertBefore(container, main.firstChild);
  }
});
