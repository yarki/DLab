import { Route } from '@angular/router';
import { HomeComponent } from './index';

import { LoggedInGuard } from '../logged-in.guard';

export const HomeRoutes: Route[] = [
  {
    path: '/dashboard',
    component: HomeComponent,
    canActivate: [LoggedInGuard]
  }
];
