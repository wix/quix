import { IHistory } from '@wix/quix-shared';
import relativeDate from '../../lib/ui/filters/relative-date';
import absoluteDate from '../../lib/ui/filters/absolute-date';
import toHumanCase from '../../lib/ui/filters/to-human-case';
import * as React from 'react';
import { RowConfig } from '../../lib/ui/components/Table';

interface HighlightedRowConfig<T> extends RowConfig<T> {
  filter?(
    value,
    item: T,
    index,
    highlight?: (string) => React.ReactNode
  ): React.ReactNode;
}

export const historyTableFields: HighlightedRowConfig<IHistory>[] = [
  {
    name: 'email',
    title: 'Email',
    filter(_, history: IHistory, index, highlight) {
      return <span>{highlight(history.email)}</span>;
    }
  },
  {
    name: 'query',
    title: 'Query',
    filter(_, history: IHistory, index, highlight) {
      const hasQuery = history.query.length > 0;
      const fullQuery = hasQuery ? history.query.join(';\n') + ';' : '';

      return (
        <span className="bi-muted bi-text--sm">
          {highlight(fullQuery)}
        </span>
      )
    }
  },
  {
    name: 'moduleType',
    title: 'Type',
    filter(_, history: IHistory, index, highlight) {
      return (
        <span className="bi-tag--sm">
          {highlight(toHumanCase()(history.moduleType))}
        </span>
      );
    }
  },
  {
    name: 'startedAt',
    title: 'Started At',
    filter(_, history: IHistory, index) {
      return (
        <div className="bi-align bi-s-h--x05 bi-text--sm bi-muted">
          <span>
            {relativeDate()(history.startedAt as any)}
          </span>

          <span>
            ({absoluteDate()(history.startedAt as any)})
          </span>
        </div>
      );
    }
  }
];
