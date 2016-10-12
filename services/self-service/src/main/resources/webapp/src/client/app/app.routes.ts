import { Routes, RouterModule, CanActivate } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { AuthorizationGuard } from './security/authorization.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'

  },
  {
    path: 'dashboard',
    component: HomeComponent,
    canActivate: [AuthorizationGuard]
  },
  {
    path: 'login',
    component: LoginComponent
  }
];
