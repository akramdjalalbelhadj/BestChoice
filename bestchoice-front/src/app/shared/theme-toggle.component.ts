import { Component, inject } from '@angular/core';
import { ThemeService } from '../core/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  template: `
    <button
      class="theme-toggle-btn"
      (click)="theme.toggle()"
      [title]="theme.isDark() ? 'Mode clair' : 'Mode sombre'">
      {{ theme.isDark() ? '☀️' : '🌙' }}
    </button>
  `,
  styles: [`
    .theme-toggle-btn {
      background: var(--bc-card);
      border: 1px solid var(--bc-border-2);
      border-radius: var(--bc-radius-md);
      padding: 8px 11px;
      cursor: pointer;
      font-size: 1.05rem;
      line-height: 1;
      transition: all var(--bc-transition);
      &:hover {
        background: var(--bc-card-hover);
        border-color: var(--bc-primary-border);
        transform: scale(1.08);
      }
    }
  `]
})
export class ThemeToggleComponent {
  theme = inject(ThemeService);
}
