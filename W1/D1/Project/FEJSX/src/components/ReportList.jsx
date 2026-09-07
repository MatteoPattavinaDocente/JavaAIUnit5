import { CATEGORIES, CATEGORY_COLOR, CATEGORY_LABEL } from '../constants'

// Prop attese: reports (Report[]), loading (boolean), error (string | null),
// category (Category | null), selectedId (number | null),
// onCategoryChange(category | null), onSelect(report).

function formatDate(iso) {
  return new Date(iso).toLocaleString('it-IT', { dateStyle: 'short', timeStyle: 'short' })
}

/** Mostra solo cio' che il server ha restituito per il viewport corrente. */
export function ReportList({
  reports,
  loading,
  error,
  category,
  selectedId,
  onCategoryChange,
  onSelect,
}) {
  return (
    <section className="card list">
      <header className="list-header">
        <h2>Nel riquadro visibile</h2>
        <span className="count">{reports.length}</span>
      </header>

      <div className="filters">
        <button
          type="button"
          className={category === null ? 'chip active' : 'chip'}
          onClick={() => onCategoryChange(null)}
        >
          Tutte
        </button>
        {CATEGORIES.map((value) => (
          <button
            key={value}
            type="button"
            className={category === value ? 'chip active' : 'chip'}
            style={{ borderColor: CATEGORY_COLOR[value] }}
            onClick={() => onCategoryChange(category === value ? null : value)}
          >
            <span className="dot" style={{ backgroundColor: CATEGORY_COLOR[value] }} />
            {CATEGORY_LABEL[value]}
          </button>
        ))}
      </div>

      {error && <p className="error">{error}</p>}
      {loading && <p className="hint">Caricamento...</p>}
      {!loading && !error && reports.length === 0 && (
        <p className="hint">Nessuna segnalazione qui. Sposta la mappa o clicca per aggiungerne una.</p>
      )}

      <ul>
        {reports.map((report) => (
          <li key={report.id}>
            <button
              type="button"
              className={report.id === selectedId ? 'item selected' : 'item'}
              onClick={() => onSelect(report)}
            >
              <span className="item-badge" style={{ backgroundColor: CATEGORY_COLOR[report.category] }}>
                {CATEGORY_LABEL[report.category]}
              </span>
              <span className="item-description">{report.description}</span>
              {report.address && <span className="item-address">{report.address}</span>}
              <time dateTime={report.createdAt}>{formatDate(report.createdAt)}</time>
            </button>
          </li>
        ))}
      </ul>
    </section>
  )
}
