import  { ComputationalResourceApplication } from "./computationalResourceApplication.model";

export class ComputationalResourceApplicationTemplate {
  version: string;
  applications: Array<ComputationalResourceApplication>

  constructor(jsonModel:any) {
    this.version = jsonModel.version;
    this.applications = [];

    if(jsonModel.applications && jsonModel.applications.length > 0)
      for (let index = 0; index < jsonModel.applications.length; index++)
        this.applications.push(new ComputationalResourceApplication(jsonModel.applications[index]));

  }
}
