import React from 'react';
import { Modal, Input, Form, Select, Avatar } from 'antd';
import {
  EditOutlined,
  CarryOutOutlined,
  FileTextOutlined,
  AccountBookOutlined
} from '@ant-design/icons';
import { connect } from 'react-redux';
import { GroupsWithOwner } from '../../features/group/interfaces';
import { updateGroups } from '../../features/group/actions';
import { IState } from '../../store';
import { Project } from '../../features/project/interfaces';
import { iconMapper } from '../../components/side-menu/side-menu.compoennt';

import './modals.styles.less';

const InputGroup = Input.Group;
const { TextArea } = Input;
const { Option } = Select;

type ProjectProps = {
  project: Project;
};

//props of groups
type GroupProps = {
  groups: GroupsWithOwner[];
  updateGroups: () => void;
};

type ModalState = {
  isShow: boolean;
  name: string;
  description: string;
  group_id: number;
};

class EditProject extends React.Component<
  ProjectProps & GroupProps,
  ModalState
> {
  componentDidMount() {
    this.props.updateGroups();
  }

  state: ModalState = {
    isShow: false,
    name: this.props.project.name,
    description: this.props.project.description,
    group_id:
      this.props.project && this.props.project.group
        ? this.props.project.group.id
        : 0
  };

  showModal = () => {
    const { name, description, group } = this.props.project;
    const group_id = group.id;
    this.setState({
      isShow: true,
      name: name,
      description: description,
      group_id: group_id
    });
  };

  updateProject = () => {
    this.setState({ isShow: false });
  };

  onCancel = () => {
    this.setState({ isShow: false });
  };

  onChangeName = (name: string) => {
    this.setState({ name: name });
  };

  onChangeDescription = (description: string) => {
    this.setState({ description: description });
  };

  onChangeGroupId = (group_id: number) => {
    this.setState({ group_id: group_id });
  };

  render() {
    const { project, groups: groupsByOwner } = this.props;
    return (
      <div className='edit-project' title='Edit Project'>
        <EditOutlined
          title='Edit Project'
          onClick={this.showModal}
          style={{ fontSize: 20 }}
        />

        <Modal
          title='Edit BuJo'
          visible={this.state.isShow}
          onCancel={this.onCancel}
          onOk={() => this.updateProject}
        >
          <Form>
            <Form.Item>
              <InputGroup compact>
                <div style={{ alignItems: 'center', width: '100%' }}>
                  <span title={`${project.projectType}`}>
                    <strong>{iconMapper[project.projectType]}</strong>
                  </span>
                  <Input
                    style={{ width: '90%', marginLeft: '20px' }}
                    placeholder='Enter BuJo Name'
                    value={this.state.name}
                    onChange={e => this.onChangeName(e.target.value)}
                  />
                </div>

                <div style={{ margin: '24px 0' }} />
                <TextArea
                  placeholder='Enter Description'
                  autoSize
                  value={this.state.description}
                  onChange={e => this.onChangeDescription(e.target.value)}
                />

                <div style={{ margin: '24px 0' }} />
                <Select
                  placeholder='Choose Group'
                  style={{ width: '100%' }}
                  value={this.state.group_id}
                  onChange={value => this.onChangeGroupId(value)}
                >
                  {groupsByOwner.map(groupsOwner => {
                    return groupsOwner.groups.map(group => (
                      <Option
                        key={`group${group.id}`}
                        value={group.id}
                        title={`Group "${group.name}" (owner "${group.owner}")`}
                      >
                        <Avatar size='small' src={group.ownerAvatar} />
                        &nbsp;&nbsp;Group <strong>
                          {group.name}
                        </strong> (owner <strong>{group.owner}</strong>)
                      </Option>
                    ));
                  })}
                </Select>
              </InputGroup>
            </Form.Item>
          </Form>
        </Modal>
      </div>
    );
  }
}

const mapStateToProps = (state: IState) => ({
  project: state.project.project,
  groups: state.group.groups
});

export default connect(mapStateToProps, { updateGroups })(EditProject);