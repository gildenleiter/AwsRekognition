import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MenuComponent } from './pages/menu/menu.component';
import { ProjectComponent } from './pages/project/project.component';
import { TrainingComponent } from './pages/training/training.component';
import { ModelComponent } from './pages/model/model.component';
import { InferenceComponent } from './pages/inference/inference.component';

const routes: Routes = [
  { path: '', redirectTo: '/menu', pathMatch: 'full' },
  { path: 'menu', component: MenuComponent  },
  { path: 'project', component: ProjectComponent },
  { path: 'training', component: TrainingComponent },
  { path: 'model', component: ModelComponent },
  { path: 'inference', component: InferenceComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
