import type { ReactNode } from 'react';
import styles from './HoverHint.module.css';

interface HoverHintProps {
  /** 마우스를 올리거나 포커스했을 때 보여줄 설명. null이면 툴팁 없이 렌더링한다. */
  hint: string | null | undefined;
  /** 래퍼의 배치(정렬 등)를 조정할 때 사용하는 추가 클래스 */
  className?: string;
  children: ReactNode;
}

/**
 * 비활성화된 버튼처럼 "왜 안 되는지" 설명이 필요한 요소를 감싸는 래퍼.
 * 비활성 버튼은 브라우저에 따라 마우스 이벤트를 받지 못하므로,
 * 래퍼가 호버를 받아 말풍선 툴팁을 보여준다.
 */
function HoverHint({ hint, className, children }: HoverHintProps) {
  const baseClassName = hint ? styles.wrapWithHint : styles.wrap;
  const wrapClassName = className ? `${baseClassName} ${className}` : baseClassName;

  return (
    <div className={wrapClassName} data-hint={hint || undefined}>
      {children}
    </div>
  );
}

export default HoverHint;
