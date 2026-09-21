const state = {
    books: [],
    category: 'Todas',
    search: ''
};

const bookGrid = document.querySelector('#book-grid');
const emptyState = document.querySelector('#empty-state');
const searchInput = document.querySelector('#search');

async function loadCatalog() {
    try {
        const [booksResponse, categoriesResponse] = await Promise.all([
            fetch('/api/livros'),
            fetch('/api/categorias')
        ]);
        if (!booksResponse.ok || !categoriesResponse.ok) throw new Error('Falha ao carregar o catálogo');
        state.books = await booksResponse.json();
        const categories = await categoriesResponse.json();
        renderCategories(categories);
        renderStats(categories.length);
        renderBooks();
    } catch (error) {
        bookGrid.innerHTML = '<p class="result-count">Não foi possível carregar o catálogo. Verifique se a aplicação está em execução.</p>';
    }
}

function renderCategories(categories) {
    const filters = document.querySelector('#category-filters');
    filters.innerHTML = ['Todas', ...categories.map(category => category.nome)]
        .map(category => `<button class="filter ${category === 'Todas' ? 'active' : ''}" data-category="${category}">${category}</button>`)
        .join('');
    filters.addEventListener('click', event => {
        const button = event.target.closest('.filter');
        if (!button) return;
        state.category = button.dataset.category;
        filters.querySelectorAll('.filter').forEach(item => item.classList.toggle('active', item === button));
        renderBooks();
    });
}

function renderStats(categoryCount) {
    document.querySelector('#book-count').textContent = state.books.length;
    document.querySelector('#available-count').textContent = state.books.filter(book => book.quantidadeDisponivel > 0).length;
    document.querySelector('#category-count').textContent = categoryCount;
}

function renderBooks() {
    const search = state.search.toLocaleLowerCase('pt-BR');
    const filtered = state.books.filter(book => {
        const matchesCategory = state.category === 'Todas' || book.categoria === state.category;
        const searchable = `${book.titulo} ${book.autores.join(' ')}`.toLocaleLowerCase('pt-BR');
        return matchesCategory && searchable.includes(search);
    });
    document.querySelector('#result-count').textContent = `${filtered.length} ${filtered.length === 1 ? 'livro encontrado' : 'livros encontrados'}`;
    emptyState.hidden = filtered.length > 0;
    bookGrid.innerHTML = filtered.map((book, index) => bookCard(book, index)).join('');
}

function bookCard(book, index) {
    const available = book.quantidadeDisponivel > 0;
    const author = book.autores.length ? book.autores.join(', ') : 'Autor não informado';
    return `<article class="book-card">
        <div class="book-cover cover-${index % 4}"><span class="cover-title">${book.titulo}</span></div>
        <span class="card-category">${book.categoria}</span>
        <h3>${book.titulo}</h3>
        <p class="author">${author}</p>
        <div class="card-footer">
            <span class="availability ${available ? '' : 'unavailable'}">${available ? `${book.quantidadeDisponivel} ${book.quantidadeDisponivel > 1 ? 'disponíveis' : 'disponível'}` : 'Indisponível'}</span>
            <span class="year">${book.anoPublicacao}</span>
        </div>
    </article>`;
}

searchInput.addEventListener('input', event => {
    state.search = event.target.value;
    renderBooks();
});

loadCatalog();
