import React from 'react';

export interface RowConfig<T> {
  name: keyof T;
  title?: string;
  className?: string;
  filter?(value, item: T, index?): React.ReactNode;
}

export interface HighlightedRowConfig<T> extends RowConfig<T> {
  filter?(
    value,
    item: T,
    index,
    highlight?: (term: string) => React.ReactNode
  ): React.ReactNode;
}

export interface RowProps {
  columns: {
    header: string;
    renderRow(cell: any): React.ReactNode;
    accessor: string;
    className: string;
  }[];
  onRowClicked(row: any): void;
  row: any;
}

export const Row = ({
  row,
  onRowClicked,
  columns,
}: RowProps) => {
  return (
    <tr
      onClick={() => onRowClicked(row)}
      data-hook="table-row"
    >
      {columns.map((column, index) => {
        return (
          <td
            key={index}
            className={'bi-table-cells bi-table-cell-' + column.accessor}
          >
            {column.renderRow(row)}
          </td>
        );
      })}
    </tr>
  );
};