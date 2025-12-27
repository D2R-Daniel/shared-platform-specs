"""Feature flag models."""

from datetime import datetime
from enum import Enum
from typing import Any, Optional

from pydantic import BaseModel, Field


class RuleOperator(str, Enum):
    """Operators for targeting rules."""

    EQUALS = "equals"
    NOT_EQUALS = "not_equals"
    CONTAINS = "contains"
    NOT_CONTAINS = "not_contains"
    STARTS_WITH = "starts_with"
    ENDS_WITH = "ends_with"
    GREATER_THAN = "greater_than"
    LESS_THAN = "less_than"
    IN = "in"
    NOT_IN = "not_in"
    MATCHES_REGEX = "matches_regex"
    PERCENTAGE = "percentage"


class TargetingRule(BaseModel):
    """Rule for targeting feature flag to specific users/contexts."""

    id: str
    attribute: str
    operator: RuleOperator
    value: Any
    enabled: bool = True


class FeatureFlag(BaseModel):
    """Feature flag definition."""

    id: str
    key: str
    name: str
    description: Optional[str] = None
    enabled: bool = False
    default_value: Any = False
    targeting_rules: list[TargetingRule] = Field(default_factory=list)
    tags: list[str] = Field(default_factory=list)
    metadata: Optional[dict[str, Any]] = None
    created_at: datetime
    updated_at: datetime
    created_by: Optional[str] = None


class Pagination(BaseModel):
    """Pagination metadata."""

    page: int
    page_size: int = Field(alias="pageSize")
    total_items: int = Field(alias="totalItems")
    total_pages: int = Field(alias="totalPages")
    has_next: Optional[bool] = Field(None, alias="hasNext")
    has_previous: Optional[bool] = Field(None, alias="hasPrevious")

    class Config:
        populate_by_name = True


class FeatureFlagListResponse(BaseModel):
    """Response containing a list of feature flags."""

    data: list[FeatureFlag]
    pagination: Pagination


class FeatureFlagEvaluation(BaseModel):
    """Result of evaluating a feature flag for a context."""

    key: str
    enabled: bool
    value: Any
    reason: str
    rule_id: Optional[str] = None


class EvaluationContext(BaseModel):
    """Context for evaluating feature flags."""

    user_id: Optional[str] = None
    email: Optional[str] = None
    tenant_id: Optional[str] = None
    roles: list[str] = Field(default_factory=list)
    attributes: dict[str, Any] = Field(default_factory=dict)
