import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GeminiService {
  // 1. COLLE TA CLÉ GROQ ICI
  private apiKey = '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';
  private apiUrl = 'https://api.groq.com/openai/v1/chat/completions';

  async generateResponse(userMessage: string, context: any): Promise<string> {
    const prompt = `
      Tu es l'assistant de l'app "Le bon choix".
      Contexte (Campagne/Projets) : ${JSON.stringify(context)}
      Question de l'étudiant : ${userMessage}
      Réponse : Sois très concis, amical et donne un conseil direct sur quel projet choisir.
    `;

    try {
      const response = await fetch(this.apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`
        },
        body: JSON.stringify({
          model: "llama-3.3-70b-versatile",
          messages: [
            { role: "system", content: "Tu es un conseiller académique expert." },
            { role: "user", content: prompt }
          ]
        })
      });

      const data = await response.json();

      if (data.error) {
        throw new Error(data.error.message);
      }

      return data.choices[0].message.content;
    } catch (error: any) {
      console.error("Erreur Groq :", error);
      return "Désolé, j'ai une petite panne de cerveau. Réessaie ?";
    }
  }
}
