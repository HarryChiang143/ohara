/*
 * Copyright 2019 is-land
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import PropTypes from 'prop-types';
import Tooltip from '@material-ui/core/Tooltip';

import * as PIPELINES from 'constants/pipelines';
import PipelineNewStream from './PipelineNewStream';
import PipelineNewConnector from './PipelineNewConnector';
import PipelineNewTopic from './PipelineNewTopic';
import { Modal } from 'components/common/Modal';
import { isEmptyStr } from 'utils/commonUtils';
import { Icon, ToolbarWrapper, FileSavingStatus } from './styles.js';
import { graph as graphPropType } from 'propTypes/pipeline';

const modalNames = {
  ADD_SOURCE_CONNECTOR: 'sources',
  ADD_SINK_CONNECTOR: 'sinks',
  ADD_STREAM: 'streams',
  ADD_TOPIC: 'topics',
};

class PipelineToolbar extends React.Component {
  static propTypes = {
    match: PropTypes.shape({
      params: PropTypes.object,
    }).isRequired,
    connectors: PropTypes.arrayOf(
      PropTypes.shape({
        className: PropTypes.string.isRequired,
        definitions: PropTypes.arrayOf(
          PropTypes.shape({
            displayName: PropTypes.string.isRequired,
            defaultValue: PropTypes.any,
          }),
        ).isRequired,
      }).isRequired,
    ).isRequired,
    graph: PropTypes.arrayOf(graphPropType).isRequired,
    updateGraph: PropTypes.func.isRequired,
    hasChanges: PropTypes.bool.isRequired,
    topics: PropTypes.array.isRequired,
    isLoading: PropTypes.bool.isRequired,
    updateCurrentTopic: PropTypes.func.isRequired,
    resetCurrentTopic: PropTypes.func.isRequired,
    currentTopic: PropTypes.object,
    workerClusterName: PropTypes.string.isRequired,
  };

  state = {
    isModalActive: false,
    sources: [],
    sinks: [],
    activeConnector: null,
    connectorType: '',
    isAddBtnDisabled: false,
    currWorker: null,
  };

  componentDidMount() {
    this.getConnectorInfo();
    this.modalChild = React.createRef();
  }

  getConnectorInfo = async () => {
    const result = this.props.connectors.map(connector => {
      const { className, definitions } = connector;
      let targetDefinition = {};

      definitions.forEach(definition => {
        const { displayName, defaultValue } = definition;

        if (
          displayName === 'version' ||
          displayName === 'revision' ||
          displayName === 'kind'
        ) {
          targetDefinition = {
            ...targetDefinition,
            [displayName]: defaultValue,
          };
        }
      });

      const { kind, version, revision } = targetDefinition;

      return {
        typeName: kind,
        className,
        version,
        revision,
      };
    });

    const sources = result.filter(
      ({ typeName, className }) =>
        typeName === 'source' &&
        !PIPELINES.CONNECTOR_FILTERS.includes(className),
    );

    const sinks = result.filter(
      ({ typeName, className }) =>
        typeName === 'sink' && !PIPELINES.CONNECTOR_FILTERS.includes(className),
    );

    this.setState({ sources, sinks }, () => {
      // If we have the supported connectors data at hand, let's set the
      // default connector so they can be rendered in connector modal

      const { activeConnector, connectorType = '' } = this.state;
      if (!activeConnector && !isEmptyStr(connectorType)) {
        this.setDefaultConnector(connectorType);
      }
    });
  };

  setDefaultConnector = connectorType => {
    if (connectorType) {
      const { connectorType: connector } = this.state;
      const activeConnector =
        connectorType === 'stream' ? connector : this.state[connector][0];

      this.setState({ activeConnector, isAddBtnDisabled: false });
    }
  };

  handleModalOpen = (modalName, connectorType) => {
    this.setState({ isModalActive: true, modalName, connectorType }, () => {
      this.setDefaultConnector(this.state.connectorType);
    });
  };

  handleModalClose = () => {
    this.setState({ isModalActive: false, activeConnector: null });

    if (this.state.modalName === 'topics') {
      this.props.resetCurrentTopic();
    }
  };

  handleConfirm = () => {
    this.modalChild.current.update();
    if (this.state.modalName === 'topics') {
      this.handleModalClose();
    }
  };

  handleTrSelect = name => {
    this.setState(prevState => {
      const { connectorType } = prevState;
      const active = prevState[connectorType].filter(
        connector => connector.className === name,
      );
      return {
        activeConnector: active[0],
      };
    });
  };

  updateAddBtnStatus = currConnector => {
    this.setState({ isAddBtnDisabled: !currConnector });
  };

  render() {
    const {
      hasChanges,
      updateGraph,
      graph,
      topics,
      currentTopic,
      updateCurrentTopic,
      workerClusterName,
    } = this.props;

    const {
      isModalActive,
      modalName,
      connectorType,
      activeConnector,
      isAddBtnDisabled,
    } = this.state;

    const { ftpSource } = PIPELINES.CONNECTOR_TYPES;

    const getModalTitle = () => {
      switch (modalName) {
        case modalNames.ADD_STREAM:
          return 'Add a new stream app';
        case modalNames.ADD_TOPIC:
          return 'Add a new topic';
        default: {
          const _connectorType = connectorType.substring(
            0,
            connectorType.length - 1,
          );
          return `Add a new ${_connectorType} connector`;
        }
      }
    };

    const getModalTestId = () => {
      switch (modalName) {
        case modalNames.ADD_STREAM:
          return 'streamapp-modal';
        case modalNames.ADD_TOPIC:
          return 'topic-modal';
        default: {
          const _connectorType = connectorType.substring(
            0,
            connectorType.length - 1,
          );
          return `${_connectorType}-connector-modal`;
        }
      }
    };

    return (
      <ToolbarWrapper>
        <Modal
          title={getModalTitle()}
          isActive={isModalActive}
          width={
            modalName === modalNames.ADD_TOPIC ||
            modalName === modalNames.ADD_STREAM
              ? '350px'
              : '600px'
          }
          handleCancel={this.handleModalClose}
          handleConfirm={this.handleConfirm}
          confirmBtnText="Add"
          showActions={true}
          isConfirmDisabled={isAddBtnDisabled}
        >
          <div data-testid={getModalTestId()}>
            {modalName === modalNames.ADD_STREAM && (
              <PipelineNewStream
                {...this.props}
                activeConnector={activeConnector}
                updateAddBtnStatus={this.updateAddBtnStatus}
                ref={this.modalChild}
                handleClose={this.handleModalClose}
              />
            )}

            {modalName === modalNames.ADD_TOPIC && (
              <PipelineNewTopic
                ref={this.modalChild}
                updateGraph={updateGraph}
                graph={graph}
                topics={topics}
                currentTopic={currentTopic}
                updateTopic={updateCurrentTopic}
                updateAddBtnStatus={this.updateAddBtnStatus}
                workerClusterName={workerClusterName}
              />
            )}

            {[
              modalNames.ADD_SOURCE_CONNECTOR,
              modalNames.ADD_SINK_CONNECTOR,
            ].includes(modalName) && (
              <PipelineNewConnector
                ref={this.modalChild}
                connectorType={connectorType}
                connectors={this.state[connectorType]}
                activeConnector={activeConnector}
                onSelect={this.handleTrSelect}
                updateGraph={updateGraph}
                graph={graph}
                updateAddBtnStatus={this.updateAddBtnStatus}
                workerClusterName={workerClusterName}
                handleClose={this.handleModalClose}
              />
            )}
          </div>
        </Modal>

        <Tooltip title="Add a source connector" enterDelay={1000}>
          <Icon
            className="fas fa-file-import"
            onClick={() =>
              this.handleModalOpen(modalNames.ADD_SOURCE_CONNECTOR, 'sources')
            }
            data-id={ftpSource}
            data-testid="toolbar-sources"
          />
        </Tooltip>

        <Tooltip title="Add a topic" enterDelay={1000}>
          <Icon
            className="fas fa-list-ul"
            onClick={() => this.handleModalOpen(modalNames.ADD_TOPIC)}
            data-id={modalNames.ADD_TOPIC}
            data-testid="toolbar-topics"
          />
        </Tooltip>

        <Tooltip title="Add a stream app" enterDelay={1000}>
          <Icon
            className="fas fa-wind"
            onClick={() =>
              this.handleModalOpen(modalNames.ADD_STREAM, 'stream')
            }
            data-id={modalNames.ADD_STREAM}
            data-testid="toolbar-streams"
          />
        </Tooltip>

        <Tooltip title="Add a sink connector" enterDelay={1000}>
          <Icon
            className="fas fa-file-export"
            onClick={() =>
              this.handleModalOpen(modalNames.ADD_SINK_CONNECTOR, 'sinks')
            }
            data-id={ftpSource}
            data-testid="toolbar-sinks"
          />
        </Tooltip>

        <FileSavingStatus>
          {hasChanges ? 'Saving...' : 'All changes saved'}
        </FileSavingStatus>
      </ToolbarWrapper>
    );
  }
}

export default PipelineToolbar;
