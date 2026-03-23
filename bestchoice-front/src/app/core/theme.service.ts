import { Injectable, signal, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private doc = inject(DOCUMENT);
  private readonly KEY = 'bc-theme';

  isDark = signal(true);

  constructor() {
    const saved = this.doc.defaultView?.localStorage.getItem(this.KEY);
    this.apply(saved ? saved === 'dark' : true);
  }

  toggle() {
    this.apply(!this.isDark());
  }

  private apply(dark: boolean) {
    this.isDark.set(dark);
    this.doc.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    this.doc.defaultView?.localStorage.setItem(this.KEY, dark ? 'dark' : 'light');
  }
}
