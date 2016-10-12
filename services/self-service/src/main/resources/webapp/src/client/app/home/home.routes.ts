import { Route } from '@angular/router';
import { HomeComponent } from './index';
import { AuthorizationGuard } from './../security/authorization.guard';

export const HomeRoutes: Route[] = [
  {
    path: '/dashboard',
    component: HomeComponent,
    canActivate: [AuthorizationGuard]
  }
];
