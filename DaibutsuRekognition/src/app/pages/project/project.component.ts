import { Component, OnInit } from '@angular/core';
import {AwsService} from '../../service/aws.service';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.css']
})

export class ProjectComponent implements OnInit {
  dataSource: any = null;
  columnName = ['id','action'];

  constructor(private awsService: AwsService) { }

  ngOnInit(): void {
    this.awsService.getProjectAll().subscribe(x => { 
      this.dataSource = x;
    });
  }

  createProject(){
    var projectNameElement = document.getElementById("projectName") as HTMLTextAreaElement;

    this.awsService.createProject(projectNameElement.value).subscribe(x => { 
        console.log('Subscriber:', x);
        var element = document.getElementById("resultLog");
        if(element != undefined) {
          element.innerHTML = x;
        }
      }
    );
  }

  deleteProject(obj:any) {
    var projectId = obj.id;
    this.awsService.deleteProject(projectId).subscribe(x => { 
      console.log('Subscriber:', x);
    });
  }
}
