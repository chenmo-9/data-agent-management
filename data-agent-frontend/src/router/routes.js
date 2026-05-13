import BaseLayout from '../layouts/BaseLayout.vue';
import Home from '../views/Home.vue';
import RunAgent from '../views/RunAgent.vue';
import GraphHistory from '../views/GraphHistory.vue';
import AgentManage from '../views/AgentManage.vue';
import ModelConfigManage from '../views/ModelConfigManage.vue';
import DatasourceManage from '../views/DatasourceManage.vue';
import AgentDatasourceManage from '../views/AgentDatasourceManage.vue';
import SemanticModelManage from '../views/SemanticModelManage.vue';
import KnowledgeManage from '../views/KnowledgeManage.vue';
import PromptTemplateManage from '../views/PromptTemplateManage.vue';

const routes = [
  {
    path: '/',
    component: BaseLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home,
      },
      { path: 'run', name: 'RunAgent', component: RunAgent },
      { path: 'graph-history', name: 'GraphHistory', component: GraphHistory },
      { path: 'agents', name: 'AgentManage', component: AgentManage },
      { path: 'models', name: 'ModelConfigManage', component: ModelConfigManage },
      { path: 'datasources', name: 'DatasourceManage', component: DatasourceManage },
      { path: 'agent-datasources', name: 'AgentDatasourceManage', component: AgentDatasourceManage },
      { path: 'semantic-models', name: 'SemanticModelManage', component: SemanticModelManage },
      { path: 'knowledge', name: 'KnowledgeManage', component: KnowledgeManage },
      { path: 'prompts', name: 'PromptTemplateManage', component: PromptTemplateManage },
    ],
  },
];

export default routes;
