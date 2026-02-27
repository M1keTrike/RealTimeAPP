export enum QuestionDifficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
  PRO = 'PRO',
}

export class QuestionOption {
  constructor(
    public id: string,
    public text: string,
  ) {}
}

export class Question {
  constructor(
    public id: string,
    public statement: string,
    public difficulty: QuestionDifficulty,
    public options: QuestionOption[] = [],
    public correctOptionId: string | null = null,
  ) {}

  isValid(): boolean {
    return this.options.length > 0 && this.correctOptionId !== null;
  }
}