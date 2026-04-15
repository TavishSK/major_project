export default function ResultTable({ data }) {
  if (!data) return null;

  return (
    <div className="table-container">
      <h2>Results</h2>

      <table>
        <thead>
          <tr>
            <th>Strategy</th>
            <th>Replicas</th>
            <th>Response</th>
            <th>Scaling Events</th>
          </tr>
        </thead>

        <tbody>
          {data.results.map((r, i) => (
            <tr
              key={i}
              className={r.strategy === data.bestStrategy ? "best" : ""}
            >
              <td>{r.strategy}</td>
              <td>{r.finalReplicas}</td>
              <td>{r.avgResponseTime.toFixed(2)}</td>
              <td>{r.scalingEvents}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>🏆 Best Strategy: {data.bestStrategy}</h3>
    </div>
  );
}