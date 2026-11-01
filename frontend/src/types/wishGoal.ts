export interface WishGoal {
  goalId: number;
  productId: number;
  productTitle: string;
  productImageUrl?: string | null;
  targetCloverPrice: number;
  currentCloverBalance: number;
  remainingClovers: number;
  createdAt?: string;
}

export interface ChildWishGoal {
  childUserId: number;
  childNickname: string;
  goal: WishGoal;
}
