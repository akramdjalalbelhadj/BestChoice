import { Component, Input } from '@angular/core';
import { GeminiService } from '../../services/gemini.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.html',
  styleUrls: ['./chatbot.scss']
})
export class Chatbot {
  @Input() contextData: any;
  isOpen = false;
  userInput = '';
  isLoading = false;
  messages: {text: string, type: 'user' | 'bot'}[] = [
    { text: "Bonjour ! Je connais vos campagnes. Est-ce un bon choix d'investir ?", type: 'bot' }
  ];

  constructor(private geminiService: GeminiService) {}

  toggleChat() { this.isOpen = !this.isOpen; }

  async send() {
    if (!this.userInput.trim()) return;

    const userMsg = this.userInput;
    this.messages.push({ text: userMsg, type: 'user' });
    this.userInput = '';
    this.isLoading = true;

    const response = await this.geminiService.generateResponse(userMsg, this.contextData);

    this.messages.push({ text: response, type: 'bot' });
    this.isLoading = false;
  }
}
