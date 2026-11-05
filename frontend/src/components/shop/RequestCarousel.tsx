import CardCarousel from '../common/CardCarousel';
import type { ReactNode } from 'react';

function RequestCarousel({ title, label = title, children }: { title: string; label?: string; children: ReactNode }) {
  return <CardCarousel title={title} label={label} itemLabel="요청" collapseDetails>{children}</CardCarousel>;
}

export default RequestCarousel;
