import { beforeEach, describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider, useTheme } from './ThemeContext.tsx';

function ThemeControl(){const {theme,toggleTheme}=useTheme();return <button onClick={toggleTheme}>{theme}</button>}

describe('ThemeProvider',()=>{
  beforeEach(()=>localStorage.clear());
  it('persists the selected theme and applies it to the document',()=>{
    render(<ThemeProvider><ThemeControl/></ThemeProvider>);
    fireEvent.click(screen.getByRole('button',{name:'light'}));
    expect(screen.getByRole('button',{name:'dark'})).toBeInTheDocument();
    expect(document.documentElement).toHaveAttribute('data-theme','dark');
    expect(localStorage.getItem('fleetflow.theme.v1')).toBe('dark');
  });
});
