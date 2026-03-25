import { Component, inject, OnInit, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
  selector: 'app-teacher-campaigns',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-campaigns.page.html',
  styleUrl: './teacher-campaigns.page.scss'
})
export class TeacherCampaignsPage implements OnInit {
  private readonly teacherService = inject(TeacherService);
  private readonly auth = inject(AuthStore);

  isLoading = signal(true);
  searchQuery = signal('');

  campaigns = this.teacherService.campaigns;

  filteredCampaigns = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const all = this.campaigns();

    if (!query) return all;

    return all.filter(c =>
      c.name.toLowerCase().includes(query) ||
      c.academicYear.toLowerCase().includes(query) ||
      c.algorithmType.toLowerCase().includes(query)
    );
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadAllData(user.userId);
      setTimeout(() => this.isLoading.set(false), 600);
    }
  }

  deleteCampaign(id: number) {
    if (confirm('Voulez-vous vraiment supprimer cette campagne de matching ? Cette action est irréversible.')) {
      this.teacherService.deleteCampaign(id).subscribe({
        next: () => console.log('Campagne supprimée'),
        error: (err) => alert('Erreur lors de la suppression')
      });
    }
  }
}
