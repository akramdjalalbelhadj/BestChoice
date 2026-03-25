import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, tap, switchMap, forkJoin, of} from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CampaignResponse, CampaignRequest } from '../models/campaign.model';
import {catchError, map} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class CampaignService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/campaigns`;

  // Signal privé pour gérer l'état
  private _campaigns = signal<CampaignResponse[]>([]);
  // Signal public exposé en lecture seule
  readonly campaigns = this._campaigns.asReadonly();

  /**
   * Indispensable pour le rafraîchissement automatique après ajout
   */
  getById(id: number): Observable<CampaignResponse> {
    return this.http.get<CampaignResponse>(`${this.API}/${id}`);
  }

  /**
   * Charge les campagnes d'un enseignant
   */
  loadByTeacher(teacherId: number): Observable<CampaignResponse[]> {
    return this.http.get<CampaignResponse[]>(`${this.API}/teacher/${teacherId}`).pipe(
      tap(list => this._campaigns.set(list))
    );
  }

  /**
   * Crée une nouvelle campagne
   */
  create(request: CampaignRequest): Observable<CampaignResponse> {
    return this.http.post<CampaignResponse>(this.API, request).pipe(
      tap(newC => this._campaigns.update(all => [newC, ...all]))
    );
  }

  /**
   * Supprime une campagne
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`).pipe(
      tap(() => this._campaigns.update(all => all.filter(c => c.id !== id)))
    );
  }
  /**
   * Helper : remplace la campagne modifiée dans la liste actuelle
   */
  private updateLocalCampaign(updated: CampaignResponse) {
    this._campaigns.update(all =>
      all.map(c => c.id === updated.id ? updated : c)
    );
  }

  createCompleteCampaign(request: CampaignRequest): Observable<CampaignResponse> {
    return this.http.post<CampaignResponse>(this.API, request).pipe(
      tap(newC => this._campaigns.update(all => [newC, ...all]))
    );
  }

  loadByStudent(studentId: number): Observable<CampaignResponse[]> {
    this._campaigns.set([]);

    return this.http.get<CampaignResponse[]>(`${this.API}/student/${studentId}`).pipe(
      tap(list => {
        console.log('Données reçues pour l\'étudiant:', list);
        this._campaigns.set(list);
      })
    );
  }

  getCampaignItems(campaignId: number): Observable<any[]> {
    return this.getById(campaignId).pipe(
      switchMap(campaign => {
        if (campaign.campaignType === 'PROJECT') {
          return this.http.get<any[]>(`${environment.apiBaseUrl}/api/projects/campaign/${campaignId}`);
        } else if (campaign.campaignType === 'SUBJECT') {
          return this.http.get<any[]>(`${environment.apiBaseUrl}/api/subjects/campaign/${campaignId}`);
        }
        return of([]);
      })
    );
  }

  getCompleteCampaign(campaignId: number): Observable<{ campaign: CampaignResponse, items: any[] }> {
    return this.getById(campaignId).pipe(
      switchMap(campaign => {
        const endpoint = campaign.campaignType === 'PROJECT' ? 'projects' : 'subjects';
        const itemsUrl = `${environment.apiBaseUrl}/api/${endpoint}/campaign/${campaignId}`;

        return this.http.get<any[]>(itemsUrl).pipe(
          map(items => ({
            campaign,
            items: Array.isArray(items) ? items : []
          })),
          catchError(err => {
            console.error('Erreur lors de la récupération des items', err);
            return of({ campaign, items: [] });
          })
        );
      })
    );
  }
}
