export class ResourceShapeModel {
  type: string;
  ram: string;
  cpu: number;

  constructor(
    jsonModel:any
  ) {
    this.type = jsonModel.Type;
    this.ram = jsonModel.Ram;
    this.cpu = jsonModel.Cpu;
  }
}
