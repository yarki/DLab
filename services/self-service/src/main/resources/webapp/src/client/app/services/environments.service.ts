import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from "rxjs";


@Injectable()
export class EnvironmentsService {
constructor(private http: Http) {}

  getEnvironmentsList() : Observable<String>{
      return this.http
          .get('app/components/grid/data.json')
          .map(res => res.json());
  }

}
