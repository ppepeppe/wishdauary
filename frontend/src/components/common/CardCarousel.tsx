import { Children, isValidElement, useEffect, useId, useLayoutEffect, useRef, useState, type FocusEvent, type KeyboardEvent, type ReactNode } from 'react';
import styles from './CardCarousel.module.css';

interface CardCarouselProps {
  title?: string;
  itemLabel?: string;
  collapseDetails?: boolean;
  label?: string;
  children: ReactNode;
}

function CardCarousel({ title, label = title ?? '목록', itemLabel = '항목', collapseDetails = false, children }: CardCarouselProps) {
  const slides = Children.toArray(children);
  const track = useRef<HTMLDivElement>(null);
  const trackId = useId();
  const [activeIndex, setActiveIndex] = useState(0);
  const multiple = slides.length > 1;
  const firstKey = isValidElement(slides[0]) ? slides[0].key : undefined;
  const [trackHeight, setTrackHeight] = useState<number>();

  function updatePosition() {
    const element = track.current;
    if (!element || !element.children.length) return;
    const left = element.getBoundingClientRect().left + 4;
    let nearest = 0;
    let distance = Infinity;
    Array.from(element.children).forEach((slide, index) => {
      const offset = Math.abs(slide.getBoundingClientRect().left - left);
      if (offset < distance) { nearest = index; distance = offset; }
    });
    setActiveIndex(nearest);
  }

  useEffect(() => {
    const element = track.current;
    if (!element) return;
    const observer = new ResizeObserver(updatePosition);
    observer.observe(element);
    updatePosition();
    return () => observer.disconnect();
  }, [slides.length]);

  useEffect(() => {
    if (!collapseDetails) return;
    Array.from(track.current?.children ?? []).forEach((slide, index) => {
      if (index !== activeIndex) {
        slide.querySelectorAll('details[open]').forEach((details) => { (details as HTMLDetailsElement).open = false; });
      }
    });
  }, [activeIndex, collapseDetails]);

  useLayoutEffect(() => {
    track.current?.scrollTo({ left: 0, behavior: 'instant' });
    setActiveIndex(0);
  }, [firstKey]);

  useLayoutEffect(() => {
    const element = track.current;
    const slide = element?.children[activeIndex];
    if (!element || !slide) return;
    const resize = () => {
      setTrackHeight(Math.ceil(slide.getBoundingClientRect().height) + 16);
      element.scrollTop = 0;
    };
    const observer = new ResizeObserver(resize);
    observer.observe(slide);
    resize();
    return () => observer.disconnect();
  }, [activeIndex, slides.length, firstKey]);

  function moveTo(index: number) {
    const element = track.current;
    const slide = element?.children[Math.max(0, Math.min(slides.length - 1, index))];
    if (!element || !slide) return;
    element.scrollTo({
      left: element.scrollLeft + slide.getBoundingClientRect().left - element.getBoundingClientRect().left - 4,
      behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth',
    });
  }

  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    if (event.target !== event.currentTarget) return;
    const targets: Record<string, number> = { ArrowLeft: activeIndex - 1, ArrowRight: activeIndex + 1, Home: 0, End: slides.length - 1 };
    if (!(event.key in targets)) return;
    event.preventDefault();
    moveTo(targets[event.key]);
  }

  function handleFocus(event: FocusEvent<HTMLDivElement>) {
    const element = track.current;
    if (!element || event.target === element) return;
    const index = Array.from(element.children).findIndex((slide) => slide.contains(event.target));
    if (index >= 0 && index !== activeIndex) {
      setActiveIndex(index);
      moveTo(index);
    }
  }

  if (slides.length === 0) return null;

  return (
    <section className={styles.section} aria-label={label} aria-roledescription="캐러셀">
      <div className={styles.heading}>
        {title ? <h2>{title}</h2> : null}
        <div className={styles.controls}>
          {multiple ? <button type="button" onClick={() => moveTo(activeIndex - 1)} disabled={activeIndex === 0}
            aria-label={`${label} 이전 ${itemLabel}`} aria-controls={trackId}><span aria-hidden="true">←</span></button> : null}
          <span className={styles.position} role="status" aria-live="polite" aria-atomic="true">{activeIndex + 1} / {slides.length}</span>
          {multiple ? <button type="button" onClick={() => moveTo(activeIndex + 1)} disabled={activeIndex >= slides.length - 1}
            aria-label={`${label} 다음 ${itemLabel}`} aria-controls={trackId}><span aria-hidden="true">→</span></button> : null}
        </div>
      </div>
      <div id={trackId} ref={track} style={{ height: trackHeight }} className={`${styles.track} ${multiple ? '' : styles.single}`}
        tabIndex={multiple ? 0 : undefined} role="group" aria-label={`${label} 카드`} onScroll={updatePosition} onKeyDown={handleKeyDown} onFocusCapture={handleFocus}>
        {slides.map((slide, index) => <div className={styles.slide} key={isValidElement(slide) ? slide.key : index}
          role="group" aria-roledescription="슬라이드" aria-label={`${index + 1} / ${slides.length}`}>{slide}</div>)}
      </div>
    </section>
  );
}

export default CardCarousel;
