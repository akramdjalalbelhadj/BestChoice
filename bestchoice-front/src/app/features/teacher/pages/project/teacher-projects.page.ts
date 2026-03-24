import { Component, inject, OnInit, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ProjectResponse } from '../../../project/models/project.model';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-projects.page.html',
  styleUrl: './teacher-projects.page.scss'
})
export class TeacherProjectsPage implements OnInit {
  private teacherService = inject(TeacherService);
  private auth = inject(AuthStore);

  isLoading = signal(true);
  searchQuery = signal('');

  // Récupération réactive des projets du service
  projects = this.teacherService.projects;

  // Filtrage intelligent (Titre, Mots-clés ou Compétences)
  filteredProjects = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.projects();

    return this.projects().filter(p =>
      p.title.toLowerCase().includes(query) ||
      p.keywords.some(k => k.toLowerCase().includes(query)) ||
      p.requiredSkills.some(s => s.toLowerCase().includes(query))
    );
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadMyProjects(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe();
    }
  }

  // Utilisation de la méthode centralisée du TeacherService
  toggleStatus(p: ProjectResponse) {
    this.teacherService.toggleProjectStatus(p.id, p.active).subscribe();
  }

  deleteProject(id: number) {
    if (confirm('Voulez-vous vraiment retirer ce projet ? Il sera archivé et ne sera plus visible.')) {
      this.teacherService.toggleProjectStatus(id, true).subscribe(); // Ou deleteProject si implémenté
    }
  }
}
