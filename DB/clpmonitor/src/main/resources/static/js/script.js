const eventSource = new EventSource('/clp-data-stream');

eventSource.addEventListener('clp1-data', function (event) {
    const data = JSON.parse(event.data);
    const byteArray = Array.isArray(data.value) ? data.value : JSON.parse(data.value);
    const grid = document.getElementById('clp1-grid');
    grid.innerHTML = '';

    byteArray.forEach((val, i) => {
        const cell = document.createElement('div');
        cell.classList.add('cell', `color-${val}`);
        cell.textContent = i + 1; // Mostra o número do bloco
        grid.appendChild(cell);
    });
});

document.getElementById("clp-write-form").addEventListener("submit", function (e) {
    e.preventDefault(); // evita recarregar a página

    const form = e.target;
    const formData = new FormData(form);

    fetch('/write-tag', {
        method: 'POST',
        body: formData
    })
});

// Adicione este novo listener
eventSource.addEventListener('expedition-data', function (event) {
    const data = JSON.parse(event.data);
    const expeditionArray = Array.isArray(data.value) ? data.value : JSON.parse(data.value);
    const grid = document.getElementById('expedition-grid');
    grid.innerHTML = '';

    expeditionArray.forEach((val, i) => {
        const cell = document.createElement('div');
        cell.classList.add('cell2', `color-${val}`);

        const formattedText = val === 0 ? "____" : `OP${val.toString().padStart(4, '0')}`;
        cell.textContent = `P${i + 1}= [${formattedText}]`;

        if (val) {
            cell.style.border = "1px solid red";
            cell.style.backgroundColor = "rgba(255, 0, 0, 0.3)";
        } else {
            cell.style.border = "1px solid green";
            cell.style.backgroundColor = "rgba(0, 255, 0, 0.3)";
        }

        grid.appendChild(cell);
    });
});

function updateEstoque() {
    fetch('/update-stock', {
        method: 'POST'
    })
}

function updateExpedicao() {
    fetch('/update-expedition', {
        method: 'POST'
    })
}

window.onload = function () {
    updateEstoque();
    updateExpedicao();
};