//import { Routes } from '@angular/router';


import { Routes, RouterModule, CanActivate } from '@angular/router';

import { LoginRoutes } from './login/index';
import { HomeRoutes } from './home/index';

import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';

import { LoggedInGuard } from './logged-in.guard';


// export const routes: Routes = [
//   ...HomeRoutes,
//   ...LoginRoutes
// ];


export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'

  },
  {
    path: 'dashboard',
    component: HomeComponent,
    canActivate: [LoggedInGuard]
  },
  {
    path: 'login',
    component: LoginComponent
  }
];
