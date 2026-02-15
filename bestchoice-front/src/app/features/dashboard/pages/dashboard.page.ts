import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { Role } from '../../../core/models/auth.model';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="app">
      <aside class="sidebar">
        <div class="brand">
          <div class="mark">
            <div class="logo">LBC</div>
          </div>
          <div class="brandText">
            <div class="name">Le Bon Choix</div>
          </div>
        </div>

        <div class="profile">
          <div class="avatar">{{ initials() }}</div>
          <div class="meta">
            <div class="who">{{ fullName() }}</div>
            <div class="tag">{{ role() }}</div>
          </div>
        </div>

        <div class="sectionLabel">Navigation</div>
        <nav class="nav">
          <a class="item active" aria-current="page">
            <span class="dot" aria-hidden="true"></span>
            <span>Vue d’ensemble</span>
          </a>

          <ng-container *ngIf="role() === 'ADMIN'">
            <a class="item" routerLink="/admin/users"><span class="dot" aria-hidden="true"></span><span>Utilisateurs</span></a>
            <a class="item" routerLink="/admin/projects"><span class="dot" aria-hidden="true"></span><span>Projets</span></a>
            <a class="item" routerLink="/admin/referentials"><span class="dot" aria-hidden="true"></span><span>Référentiels</span></a>
            <a class="item" routerLink="/admin/matching"><span class="dot" aria-hidden="true"></span><span>Matching</span></a>
            <a class="item" routerLink="/admin/stats"><span class="dot" aria-hidden="true"></span><span>Statistiques</span></a>
          </ng-container>

          <ng-container *ngIf="role() === 'ETUDIANT'">
            <a class="item" routerLink="/student/reco"><span class="dot" aria-hidden="true"></span><span>Recommandations</span></a>
            <a class="item" routerLink="/student/preferences"><span class="dot" aria-hidden="true"></span><span>Préférences</span></a>
            <a class="item" routerLink="/student/profile"><span class="dot" aria-hidden="true"></span><span>Mon profil</span></a>
            <a class="item" routerLink="/student/chat"><span class="dot" aria-hidden="true"></span><span>Assistant</span></a>
          </ng-container>

          <ng-container *ngIf="role() === 'ENSEIGNANT'">
            <a class="item" routerLink="/teacher/projects"><span class="dot" aria-hidden="true"></span><span>Mes projets</span></a>
            <a class="item" routerLink="/teacher/preferences"><span class="dot" aria-hidden="true"></span><span>Préférences reçues</span></a>
            <a class="item" routerLink="/teacher/matching"><span class="dot" aria-hidden="true"></span><span>Résultats</span></a>
          </ng-container>
        </nav>

        <div class="spacer"></div>

        <button class="btn secondary" (click)="logout()">Déconnexion</button>
        <div class="foot">© BestChoice</div>
      </aside>

      <main class="main">
        <header class="topbar">
          <div class="headline">
            <div class="eyebrow">Tableau de bord</div>
            <h1>Bonjour, {{ firstName() }}</h1>
            <p class="muted">{{ welcome() }}</p>
          </div>

          <div class="actions">
            <a class="btn secondary" routerLink="/auth/login">Changer de compte</a>
            <a class="btn primary" *ngIf="role() === 'ADMIN'" routerLink="/admin/matching">Lancer matching</a>
            <a class="btn primary" *ngIf="role() === 'ETUDIANT'" routerLink="/student/reco">Voir recommandations</a>
            <a class="btn primary" *ngIf="role() === 'ENSEIGNANT'" routerLink="/teacher/projects">Mes projets</a>
          </div>
        </header>

        <section class="kpi">
          <div class="kpiCard">
            <div class="k">Utilisateurs</div>
            <div class="v">—</div>
            <div class="h">Admins / enseignants / étudiants</div>
          </div>
          <div class="kpiCard">
            <div class="k">Projets actifs</div>
            <div class="v">—</div>
            <div class="h">Offres publiées & ouvertes</div>
          </div>
          <div class="kpiCard">
            <div class="k">Préférences</div>
            <div class="v">—</div>
            <div class="h">Choix enregistrés</div>
          </div>
          <div class="kpiCard">
            <div class="k">Dernier matching</div>
            <div class="v">—</div>
            <div class="h">Algorithme / session / statut</div>
          </div>
        </section>

        <section class="grid">
          <div class="panel">
            <div class="panelHead">
              <div>
                <div class="panelTitle">Actions rapides</div>
                <div class="panelSub muted">Raccourcis les plus utilisés</div>
              </div>
              <span class="badge">{{ role() }}</span>
            </div>

            <div class="quick">
              <button class="tile" *ngIf="role() === 'ADMIN'">Créer un utilisateur</button>
              <button class="tile" *ngIf="role() === 'ADMIN'">Ajouter une compétence</button>
              <button class="tile" *ngIf="role() === 'ADMIN'">Lancer le matching</button>
              <button class="tile" *ngIf="role() === 'ADMIN'">Exporter un rapport</button>

              <button class="tile" *ngIf="role() === 'ETUDIANT'">Compléter mon profil</button>
              <button class="tile" *ngIf="role() === 'ETUDIANT'">Gérer mes préférences</button>
              <button class="tile" *ngIf="role() === 'ETUDIANT'">Ouvrir l’assistant</button>

              <button class="tile" *ngIf="role() === 'ENSEIGNANT'">Publier un projet</button>
              <button class="tile" *ngIf="role() === 'ENSEIGNANT'">Voir préférences reçues</button>
              <button class="tile" *ngIf="role() === 'ENSEIGNANT'">Mettre à jour mes projets</button>
            </div>
          </div>

          <div class="panel">
            <div class="panelHead">
              <div>
                <div class="panelTitle">À surveiller</div>
                <div class="panelSub muted">Qualité des données & complétude</div>
              </div>
              <span class="badge subtle">Data</span>
            </div>

            <div class="list">
              <div class="row">
                <div class="bullet" aria-hidden="true"></div>
                <div class="rowBody">
                  <div class="t">Étudiants sans préférences</div>
                  <div class="s muted">Relance avant la date limite</div>
                </div>
                <div class="r muted">—</div>
              </div>

              <div class="row">
                <div class="bullet" aria-hidden="true"></div>
                <div class="rowBody">
                  <div class="t">Projets incomplets</div>
                  <div class="s muted">Mots-clés / compétences manquants</div>
                </div>
                <div class="r muted">—</div>
              </div>

              <div class="row">
                <div class="bullet" aria-hidden="true"></div>
                <div class="rowBody">
                  <div class="t">Profils incomplets</div>
                  <div class="s muted">Skills / centres d’intérêt</div>
                </div>
                <div class="r muted">—</div>
              </div>
            </div>
          </div>
        </section>

        <section class="wide">
          <div class="panel">
            <div class="panelHead">
              <div>
                <div class="panelTitle">Activité récente</div>
                <div class="panelSub muted">Historique simplifié</div>
              </div>
              <span class="badge subtle">Live</span>
            </div>

            <div class="timeline">
              <div class="ev">
                <div class="time muted">Maintenant</div>
                <div class="desc">
                  <div class="t">Connexion réussie</div>
                  <div class="s muted">JWT stocké côté navigateur</div>
                </div>
              </div>

              <div class="ev" *ngIf="role() === 'ADMIN'">
                <div class="time muted">À faire</div>
                <div class="desc">
                  <div class="t">Lancer le matching</div>
                  <div class="s muted">HYBRID + persist = true</div>
                </div>
              </div>

              <div class="ev">
                <div class="time muted">Ensuite</div>
                <div class="desc">
                  <div class="t">Brancher les KPI</div>
                  <div class="s muted">Endpoints stats: users/projects/preferences/matching</div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  `,
  styles: [`
    :host{
      /* Light, clean, smooth */
      --bg: #f7f8fb;
      --panel: #ffffff;
      --panel2: #fbfbfd;
      --border: rgba(15, 23, 42, .10);
      --border2: rgba(15, 23, 42, .14);
      --text: #0f172a;
      --muted: rgba(15, 23, 42, .62);
      --muted2: rgba(15, 23, 42, .48);
      --shadow: 0 20px 60px rgba(2, 6, 23, .08);
      --shadow2: 0 10px 28px rgba(2, 6, 23, .07);
      --radius: 18px;
      --radius2: 14px;
      --focus: 0 0 0 6px rgba(15, 23, 42, .06);

      color: var(--text);
      display:block;
      min-height:100vh;
      background:
        radial-gradient(900px 600px at 10% 0%, rgba(15,23,42,.06), transparent 55%),
        radial-gradient(900px 600px at 90% 10%, rgba(15,23,42,.05), transparent 55%),
        linear-gradient(180deg, #fbfcff, var(--bg));
      font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji","Segoe UI Emoji";
    }

    .app{min-height:100vh;display:grid;grid-template-columns:300px 1fr}

    /* Sidebar */
    .sidebar{
      padding:18px;
      border-right:1px solid var(--border);
      background: rgba(255,255,255,.70);
      backdrop-filter: blur(10px);
    }

    .brand{display:flex;gap:12px;align-items:center;margin-bottom:18px}
    .mark{
      width:44px;height:44px;border-radius:14px;
      background: linear-gradient(180deg, rgba(15,23,42,.08), rgba(15,23,42,.02));
      border:1px solid var(--border);
      display:grid;place-items:center;
      box-shadow: var(--shadow2);
    }
    .logo{
      width:36px;height:36px;border-radius:12px;
      display:grid;place-items:center;
      background: #0f172a;
      color:#fff;
      font-weight:900;
      letter-spacing:-.02em;
    }
    .name{font-weight:850;letter-spacing:-.02em}
    .sub{font-size:12px;color:var(--muted)}

    .profile{
      display:flex;gap:12px;align-items:center;
      padding:12px;
      border:1px solid var(--border);
      border-radius: var(--radius);
      background: var(--panel);
      box-shadow: var(--shadow2);
      margin-bottom:14px;
    }
    .avatar{
      width:44px;height:44px;border-radius:14px;
      display:grid;place-items:center;
      background: rgba(15,23,42,.06);
      border:1px solid var(--border);
      font-weight:900;
      letter-spacing:-.02em;
    }
    .who{font-weight:850;letter-spacing:-.01em}
    .tag{
      margin-top:4px;
      width:max-content;
      font-size:12px;
      padding:5px 10px;
      border-radius:999px;
      border:1px solid var(--border);
      background: rgba(15,23,42,.04);
      color: var(--muted);
    }

    .sectionLabel{
      margin:14px 2px 8px;
      font-size:11px;
      letter-spacing:.12em;
      text-transform:uppercase;
      color: var(--muted2);
    }

    .nav{display:grid;gap:6px}
    .item{
      display:flex;align-items:center;gap:10px;
      padding:10px 12px;
      border-radius:14px;
      text-decoration:none;
      color: var(--text);
      border:1px solid transparent;
      background: transparent;
      transition: background .15s ease, border-color .15s ease, transform .06s ease;
    }
    .item:hover{background: rgba(15,23,42,.04);border-color: var(--border);transform: translateY(-1px)}
    .active{background: rgba(15,23,42,.06);border-color: var(--border2)}
    .dot{
      width:6px;height:6px;border-radius:999px;
      background: rgba(15,23,42,.25);
    }
    .active .dot{background: rgba(15,23,42,.75)}

    .spacer{height:16px}

    /* Buttons */
    .btn{
      padding:10px 12px;
      border-radius:14px;
      border:1px solid var(--border);
      background: var(--panel);
      color: var(--text);
      font-weight:800;
      cursor:pointer;
      text-decoration:none;
      display:inline-flex;
      align-items:center;
      justify-content:center;
      gap:10px;
      box-shadow: 0 6px 18px rgba(2,6,23,.05);
      transition: transform .06s ease, box-shadow .15s ease, background .15s ease, border-color .15s ease;
    }
    .btn:hover{transform: translateY(-1px);box-shadow: 0 10px 26px rgba(2,6,23,.08);border-color: var(--border2)}
    .btn:active{transform: translateY(0);box-shadow: 0 6px 18px rgba(2,6,23,.05)}
    .btn:focus-visible{outline:none;box-shadow: var(--focus), 0 10px 26px rgba(2,6,23,.08)}
    .primary{
      background: #0f172a;
      color:#fff;
      border-color: transparent;
    }
    .primary:hover{background: #0b1224}
    .secondary{background: var(--panel)}
    .foot{margin-top:10px;font-size:12px;color:var(--muted2);text-align:center}

    /* Main */
    .main{padding:22px 22px 40px;max-width:1200px}
    .topbar{
      display:flex;justify-content:space-between;gap:16px;align-items:flex-start;
      padding:16px;
      border:1px solid var(--border);
      border-radius: var(--radius);
      background: var(--panel);
      box-shadow: var(--shadow);
      margin-bottom:14px;
    }
    .eyebrow{
      font-size:11px;letter-spacing:.12em;text-transform:uppercase;color:var(--muted2);
      margin-bottom:6px;
    }
    h1{margin:0 0 6px;font-size:28px;letter-spacing:-.03em}
    .muted{color: var(--muted);margin:0}
    .actions{display:flex;gap:10px;flex-wrap:wrap;justify-content:flex-end}

    /* KPI */
    .kpi{display:grid;grid-template-columns:repeat(4, minmax(0,1fr));gap:12px;margin-bottom:12px}
    .kpiCard{
      padding:14px;
      border-radius: var(--radius);
      border:1px solid var(--border);
      background: var(--panel);
      box-shadow: var(--shadow2);
      transition: transform .08s ease, border-color .15s ease, box-shadow .15s ease;
    }
    .kpiCard:hover{transform: translateY(-1px);border-color: var(--border2);box-shadow: 0 16px 40px rgba(2,6,23,.09)}
    .k{color: var(--muted2);font-size:12px}
    .v{font-size:28px;font-weight:900;margin-top:6px;letter-spacing:-.03em}
    .h{color: var(--muted);font-size:12px;margin-top:2px}

    /* Panels */
    .grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    .wide{margin-top:12px}
    .panel{
      padding:14px;
      border-radius: var(--radius);
      border:1px solid var(--border);
      background: var(--panel);
      box-shadow: var(--shadow2);
    }
    .panelHead{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}
    .panelTitle{font-weight:900;letter-spacing:-.02em}
    .panelSub{font-size:12px;margin-top:2px}
    .badge{
      font-size:12px;
      padding:6px 10px;
      border-radius:999px;
      border:1px solid var(--border);
      background: rgba(15,23,42,.04);
      color: var(--text);
    }
    .subtle{color: var(--muted);}

    .quick{display:grid;grid-template-columns:1fr 1fr;gap:10px}
    .tile{
      padding:12px;
      border-radius:16px;
      border:1px solid var(--border);
      background: var(--panel2);
      color: var(--text);
      cursor:pointer;
      text-align:left;
      font-weight:800;
      transition: transform .06s ease, box-shadow .15s ease, border-color .15s ease, background .15s ease;
    }
    .tile:hover{transform: translateY(-1px);border-color: var(--border2);box-shadow: 0 12px 26px rgba(2,6,23,.08);background: #fff}
    .tile:active{transform: translateY(0)}
    .tile:focus-visible{outline:none;box-shadow: var(--focus), 0 12px 26px rgba(2,6,23,.08)}

    .list{display:grid;gap:10px}
    .row{
      display:grid;
      grid-template-columns:10px 1fr auto;
      gap:12px;
      align-items:center;
      padding:12px;
      border-radius:16px;
      border:1px solid var(--border);
      background: var(--panel2);
    }
    .bullet{
      width:8px;height:8px;border-radius:999px;
      background: rgba(15,23,42,.30);
    }
    .rowBody .t{font-weight:850}
    .rowBody .s{font-size:12px}
    .r{font-variant-numeric: tabular-nums}

    .timeline{display:grid;gap:10px}
    .ev{
      display:grid;
      grid-template-columns:90px 1fr;
      gap:12px;
      padding:12px;
      border-radius:16px;
      border:1px solid var(--border);
      background: var(--panel2);
    }
    .time{font-size:12px}
    .desc .t{font-weight:850}
    .desc .s{font-size:12px}

    @media (max-width: 1100px){
      .kpi{grid-template-columns:repeat(2, minmax(0,1fr))}
    }
    @media (max-width: 920px){
      .app{grid-template-columns:1fr}
      .sidebar{position:sticky;top:0;z-index:5;border-right:0;border-bottom:1px solid var(--border)}
    }
    @media (max-width: 640px){
      .grid{grid-template-columns:1fr}
      .actions{justify-content:flex-start}
      h1{font-size:24px}
      .app{min-height:auto}
    }
  `]
})
export class DashboardPage {
  private store = inject(AuthStore);
  private router = inject(Router);

  role = computed<Role>(() => this.store.user()?.role ?? 'ADMIN');
  firstName = computed(() => this.store.user()?.firstName ?? 'Admin');

  fullName = computed(() => {
    const u = this.store.user();
    return `${u?.firstName ?? 'Admin'} ${u?.lastName ?? ''}`.trim();
  });

  initials = computed(() => {
    const u = this.store.user();
    const a = (u?.firstName ?? 'A')[0] ?? 'A';
    const b = (u?.lastName ?? 'D')[0] ?? 'D';
    return (a + b).toUpperCase();
  });

  welcome = computed(() => {
    const r = this.role();
    if (r === 'ADMIN') return "Supervisez les utilisateurs et projets, et pilotez l’algorithme de matching.";
    if (r === 'ENSEIGNANT') return "Publiez vos projets et analysez les préférences ainsi que les résultats de matching.";
    return "Complétez votre profil et recevez des recommandations adaptées à votre parcours.";
  });

  logout() {
    this.store.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
