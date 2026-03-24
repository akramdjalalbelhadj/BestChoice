import { Injectable, inject, signal, computed } from '@angular/core';
import {forkJoin, of, tap} from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProjectService } from '../../project/services/project.service';
import { SubjectService } from '../../subject/services/subject.service';
import { CampaignService } from '../../campaign/services/campaign.service';
import { ProjectResponse, ProjectCreateRequest } from '../../project/models/project.model';
import { SubjectResponse, SubjectCreateRequest } from '../../subject/models/subject.model';
import { CampaignResponse, CampaignRequest } from '../../campaign/models/campaign.model';
import { StudentService } from '../../student/services/student.service';
import {StudentResponse} from '../../student/models/student.model';

@Injectable({ providedIn: 'root' })
export class TeacherService {
  private projectService = inject(ProjectService);
  private subjectService = inject(SubjectService);
  private campaignService = inject(CampaignService);
  private studentService = inject(StudentService);

  private readonly _projects = signal<ProjectResponse[]>([]);
  private readonly _subjects = signal<SubjectResponse[]>([]);
  private readonly _campaigns = signal<CampaignResponse[]>([]);
  private readonly _allStudents = signal<StudentResponse[]>([]);

  readonly projects = this._projects.asReadonly();
  readonly subjects = this._subjects.asReadonly();
  readonly campaigns = this._campaigns.asReadonly();
  readonly allStudents = this._allStudents.asReadonly();

  readonly totalItemsCount = computed(() => this._projects().length + this._subjects().length);

  /**
   * Charge toutes les données de l'enseignant au démarrage du Dashboard
   */
  loadAllData(teacherId: number) {
    forkJoin({
      projects: this.projectService.getByTeacher(teacherId).pipe(catchError(() => of([]))),
      subjects: this.subjectService.getByTeacher(teacherId).pipe(catchError(() => of([]))),
      campaigns: this.campaignService.loadByTeacher(teacherId).pipe(catchError(() => of([]))),
      students: this.studentService.getAllStudents().pipe(catchError(() => of([])))
    }).subscribe({
      next: (res) => {
        this._projects.set(res.projects);
        this._subjects.set(res.subjects);
        this._allStudents.set(res.students);
        console.log("Données chargées :", res.students.length, "étudiants trouvés.");
      }
    });
  }

  getProjectById(id: number) { return this.projectService.getById(id); }


  // ==================== GESTION DES PROJETS ====================

  createProject(teacherId: number, req: ProjectCreateRequest) {
    return this.projectService.create(teacherId, req).pipe(
      tap(newP => this._projects.update(all => [newP, ...all]))
    );
  }

  toggleProjectStatus(id: number, currentStatus: boolean) {
    const action$ = currentStatus ? this.projectService.deactivate(id) : this.projectService.activate(id);
    return action$.pipe(
      tap(() => this._projects.update(all =>
        all.map(p => p.id === id ? { ...p, active: !currentStatus } : p)
      ))
    );
  }

  // ==================== GESTION DES MATIÈRES OPTIONNELLES ====================

  createSubject(teacherId: number, req: SubjectCreateRequest) {
    return this.subjectService.create(teacherId, req).pipe(
      tap(newS => this._subjects.update(all => [newS, ...all]))
    );
  }

  toggleSubjectStatus(id: number, currentStatus: boolean) {
    const action$ = currentStatus ? this.subjectService.deactivate(id) : this.subjectService.activate(id);
    return action$.pipe(
      tap(() => this._subjects.update(all =>
        all.map(s => s.id === id ? { ...s, active: !currentStatus } : s)
      ))
    );
  }

  // ==================== GESTION DES CAMPAGNES ====================

  createCampaign(req: CampaignRequest) {
    return this.campaignService.create(req);
  }

  deleteCampaign(id: number) {
    return this.campaignService.delete(id);
  }

  /**
   * Charge les projets de l'enseignant et synchronise le Signal global.
   * On retourne l'Observable pour permettre au composant de gérer le 'isLoading'.
   */
  loadMyProjects(teacherId: number) {
    return this.projectService.getByTeacher(teacherId).pipe(
      tap(projs => {
        this._projects.set(projs);
      })
    );
  }
}
