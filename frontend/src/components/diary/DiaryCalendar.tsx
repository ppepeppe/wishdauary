import { useMemo, useState } from 'react';
import { getDateKey, getTodayDateInKorea } from '../../constants/date';
import type { DiaryEntry } from '../../types/diary';
import styles from './DiaryCalendar.module.css';

interface DiaryCalendarProps {
  diaries: DiaryEntry[];
  selectedDate: string | null;
  onSelectDate: (date: string | null) => void;
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

function getDiaryDate(diary: DiaryEntry) {
  return getDateKey(diary.writtenDate ?? diary.createdAt);
}

function formatMonthKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

function DiaryCalendar({ diaries, selectedDate, onSelectDate }: DiaryCalendarProps) {
  const todayDate = getTodayDateInKorea();
  const [visibleMonth, setVisibleMonth] = useState(() => {
    const today = new Date(`${todayDate}T00:00:00`);
    return new Date(today.getFullYear(), today.getMonth(), 1);
  });

  const diaryCountByDate = useMemo(() => {
    const countMap = new Map<string, number>();

    diaries.forEach((diary) => {
      const date = getDiaryDate(diary);
      if (date) {
        countMap.set(date, (countMap.get(date) ?? 0) + 1);
      }
    });

    return countMap;
  }, [diaries]);

  const year = visibleMonth.getFullYear();
  const month = visibleMonth.getMonth();
  const monthKey = formatMonthKey(visibleMonth);
  const firstWeekday = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();
  const cells = Array.from({ length: firstWeekday + lastDate }, (_, index) => {
    if (index < firstWeekday) {
      return null;
    }

    const day = index - firstWeekday + 1;
    return `${monthKey}-${String(day).padStart(2, '0')}`;
  });
  const isCurrentOrFutureMonth = monthKey >= todayDate.slice(0, 7);

  function moveMonth(offset: number) {
    setVisibleMonth((current) => new Date(current.getFullYear(), current.getMonth() + offset, 1));
    onSelectDate(null);
  }

  return (
    <section className={styles.calendar} aria-label="일기 달력">
      <div className={styles.header}>
        <button type="button" className={styles.monthButton} onClick={() => moveMonth(-1)} aria-label="이전 달">
          ‹
        </button>
        <strong>{year}년 {month + 1}월</strong>
        <button
          type="button"
          className={styles.monthButton}
          disabled={isCurrentOrFutureMonth}
          onClick={() => moveMonth(1)}
          aria-label="다음 달"
        >
          ›
        </button>
        <button
          type="button"
          className={selectedDate ? styles.allButton : styles.allButtonActive}
          onClick={() => onSelectDate(null)}
        >
          전체
        </button>
      </div>

      <div className={styles.weekdays} aria-hidden="true">
        {WEEKDAYS.map((weekday) => <span key={weekday}>{weekday}</span>)}
      </div>

      <div className={styles.days}>
        {cells.map((date, index) => {
          if (!date) {
            return <span key={`empty-${index}`} />;
          }

          const diaryCount = diaryCountByDate.get(date) ?? 0;
          const isSelected = selectedDate === date;
          const isToday = date === todayDate;

          return (
            <button
              key={date}
              type="button"
              className={isSelected ? styles.daySelected : isToday ? styles.dayToday : styles.day}
              disabled={diaryCount === 0}
              onClick={() => onSelectDate(date)}
              aria-label={`${date}, 일기 ${diaryCount}개`}
            >
              <span>{Number(date.slice(-2))}</span>
              {diaryCount > 0 ? <i>{diaryCount}</i> : null}
            </button>
          );
        })}
      </div>
    </section>
  );
}

export default DiaryCalendar;
