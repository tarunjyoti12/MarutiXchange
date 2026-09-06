// CarStatusTag.jsx
// Place at: src/components/CarStatusTag.jsx
// Usage: <CarStatusTag status={car.listingStatus} />

const STATUS_CONFIG = {
  AVAILABLE: {
    label: "Available",
    background: "#EAF3DE",
    color: "#27500A",
    dot: "#3B6D11",
  },
  RESERVED: {
    label: "Reserved",
    background: "#E6F1FB",
    color: "#0C447C",
    dot: "#185FA5",
  },
  BOOKED: {
    label: "Booked",
    background: "#FAEEDA",
    color: "#633806",
    dot: "#854F0B",
  },
  SOLD: {
    label: "Sold",
    background: "#D3D1C7",
    color: "#2C2C2A",
    dot: "#5F5E5A",
  },
};

export default function CarStatusTag({ status }) {
  const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.AVAILABLE;

  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 5,
        padding: "4px 10px",
        borderRadius: 99,
        fontSize: 11,
        fontWeight: 600,
        background: cfg.background,
        color: cfg.color,
      }}
    >
      <span
        style={{
          width: 6,
          height: 6,
          borderRadius: "50%",
          background: cfg.dot,
          display: "inline-block",
          flexShrink: 0,
        }}
      />
      {cfg.label}
    </span>
  );
}