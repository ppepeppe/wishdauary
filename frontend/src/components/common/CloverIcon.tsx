function CloverIcon() {
  return (
    <svg width="1em" height="1em" viewBox="0 0 24 26" fill="currentColor" aria-hidden="true" focusable="false">
      <path d="M12 11c2 5 2 9-1 13" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <circle cx="12" cy="11" r="1.5" />
      <g transform="rotate(45 12 11)">
        {[0, 90, 180, 270].map((angle) => (
          <path key={angle} transform={`rotate(${angle} 12 11)`} d="M12 9.5C10 8 7 6.8 7 4.8 7 2 10.5 1.2 12 3.5 13.5 1.2 17 2 17 4.8c0 2-3 3.2-5 4.7Z" />
        ))}
      </g>
    </svg>
  );
}

export default CloverIcon;
