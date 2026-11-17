import type { LinkProps } from 'react-router-dom';
import { Link } from 'react-router-dom';
import type { ButtonSize, ButtonVariant } from './Button';
import styles from './Button.module.css';

interface LinkButtonProps extends LinkProps {
  variant?: ButtonVariant;
  size?: ButtonSize;
  fullWidth?: boolean;
}

export default function LinkButton({ variant = 'primary', size = 'md', fullWidth = false, className, ...props }: LinkButtonProps) {
  const classes = [styles.button, styles[variant], styles[size], fullWidth ? styles.fullWidth : '', className].filter(Boolean).join(' ');
  return <Link {...props} className={classes} />;
}
