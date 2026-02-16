import { Component, effect, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
    standalone: true,
    template: `<p style="padding:16px">Redirection...</p>`,
})
export class DashboardPage {
    private auth = inject(AuthStore);
    private router = inject(Router);

    constructor() {
        effect(() => {
            const role = this.auth.role();
            const target = this.auth.homeUrlForRole(role);
            this.router.navigateByUrl(target);
        });
    }
}
